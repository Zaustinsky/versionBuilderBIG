package org.lwo.version;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.Issue;
import lombok.extern.slf4j.Slf4j;
import org.lwo.version.redmine.RedmineConnector;
import org.lwo.version.svn.SubversionConnector;
import org.lwo.version.svn.SvnObject;
import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Builder {


    static String mainFolder = "d:/versions";


    public static void main(String... arg) throws RedmineException, SVNException, IOException {
        String version = "2.303.245.1";
        //    Set<Integer> issueIds = Set.of(74974,69304);
        Set<Integer> issueIds = Set.of(75435);
        buildVersion(version, issueIds);
    }

    public static void buildVersion(String version, Set<Integer> issueIds) throws SVNException, RedmineException, IOException {
        Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, RC4, DH keySize < 1024, EC keySize < 224, DES40_CBC, RC4_40, 3DES_EDE_CBC");
        Security.setProperty("jdk.certpath.disabledAlgorithms", "MD2, SHA1 jdkCA & usage TLSServer, RSA keySize < 1024, DSA keySize < 1024, EC keySize < 224");
        SubversionConnector subversionConnector = new SubversionConnector();

        log.info("------ Берем заявки из редмайна");
        List<Issue> redmineIssues = new RedmineConnector().getChangesetsByIssueIds(issueIds);
        log.info("------ Взяли заявки из редмайна. Получено {} заявок из {}.", redmineIssues.size(), issueIds.size());


        log.info("------ Берем данные о ревизиях из SVN");
        List<SvnObject> versionSvnObjects = new ArrayList<>();
        for (Issue issue : redmineIssues) {
            log.info("Задача id={} Статус={} Назначена={} Тема={}", issue.getId(), issue.getStatusName(), issue.getAssigneeName(), issue.getSubject());
            List<Long> revisionIds = getRevisionIdsFromIssue(issue);
            List<SvnObject> issueSvnObjects = subversionConnector.readObjects(revisionIds);
            versionSvnObjects.addAll(issueSvnObjects);
        }
        log.info("------ Всего {} объектов собрано. Отбираем уникальные с последней ревизией по всем заявкам", versionSvnObjects.size());

        Collection<SvnObject> uniqueObjects = filterUniqueObjects(versionSvnObjects);
        log.info("------ Уникальных объектов {}", uniqueObjects.size());
        uniqueObjects.forEach(o -> log.info("{} {} {} {}", o.getRevisionDiff(), o.getLatestRevision(), o.getRevision(), o.getPath()));

        String versionFolder = mainFolder + "/" + version;
        String objFolder = versionFolder + "/obj";
        Files.createDirectory(Path.of(versionFolder));
        Files.createDirectory(Path.of(objFolder));
        log.info("------ Сохраняем файлы в {}", objFolder);
        subversionConnector.storeFiles(uniqueObjects, objFolder);
        log.info("------ Версия сохранена в {}", objFolder);

        //todo the latest revision check
        // Homework
        //todo functions ????
        //todo two catalogs - one for actual revisions second for the latest
    }

    private static Collection<SvnObject> filterUniqueObjects(List<SvnObject> versionSvnObjects) {
        Map<String, SvnObject> uniqueObjects = new HashMap<>();
        for (SvnObject object : versionSvnObjects) {
            if (uniqueObjects.containsKey(object.getPath())) {
                SvnObject existingObject = uniqueObjects.get(object.getPath());
                if (object.getRevision() > existingObject.getRevision()) {
                    uniqueObjects.put(object.getPath(), object);
                }
            } else {
                uniqueObjects.put(object.getPath(), object);
            }
        }
        return uniqueObjects.values().stream()
//                .filter(excludeSvnObjectPredicate())
                .collect(Collectors.toList());
    }

    private static Predicate<SvnObject> excludeSvnObjectPredicate() {
        return obj -> !obj.getPath().startsWith("/Belinkasgroup/branches");
    }

    private static List<Long> getRevisionIdsFromIssue(Issue issue) {
        return issue.getChangesets().stream()
                .map(Changeset::getRevision)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
