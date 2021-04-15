package org.lwo.version;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.Issue;
import lombok.extern.slf4j.Slf4j;
import org.lwo.version.bars.PropertiesBuilder;
import org.lwo.version.bars.RtfBuilder;
import org.lwo.version.bars.Zipper;
import org.lwo.version.readme.ReadmeBuilder;
import org.lwo.version.redmine.RedmineConnector;
import org.lwo.version.svn.JarUtils;
import org.lwo.version.svn.SubversionConnector;
import org.lwo.version.svn.SvnObject;
import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;
import java.net.URISyntaxException;
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
        String versionId = "4746";
        //   Set<Integer> issueIds = new TreeSet<>(List.of(61954, 63312, 73672, 73676, 73827, 73940, 74104, 74170, 74205, 74229, 75019, 75246, 75435, 60997, 61316, 65114, 67989, 74845));
        //  Set<Integer> issueIds = Set.of(78703, 78764, 78770, 78839, 78878, 78897, 78922, 78994, 79069, 79109, 79240, 79304, 67730, 76867, 78008, 78340, 78530, 78871, 79006, 79067, 79072, 79091, 79198, 79536, 61238, 75866, 79223, 79384);
//       Set<Integer> issueIds = Set.of(67768, 71205, 78409, 78604, 58580, 73533, 78047, 79112, 79204, 79388, 79493, 79502, 79890, 80128, 80142, 80421, 80538, 80573, 80689, 80706, 80807, 81055, 80246, 80871, 80735, 81020);
        Set<Integer> issueIds = Set.of();
        buildVersion(versionId, issueIds);
    }

    public static void buildVersion(String versionId, Set<Integer> issueIds) throws SVNException, RedmineException, IOException {
        Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, RC4, DH keySize < 1024, EC keySize < 224, DES40_CBC, RC4_40, 3DES_EDE_CBC");
        Security.setProperty("jdk.certpath.disabledAlgorithms", "MD2, SHA1 jdkCA & usage TLSServer, RSA keySize < 1024, DSA keySize < 1024, EC keySize < 224");
        SubversionConnector subversionConnector = new SubversionConnector();

        List<Issue> redmineIssues;
        String versionName;
        RedmineConnector redmineConnector = new RedmineConnector();
        if (issueIds == null || issueIds.isEmpty()) {
            log.info("------ Берем заявки из редмайна по версии id={}", versionId);
            redmineIssues = redmineConnector.getIssuesByVersion(versionId);
            if (redmineIssues.size() == 0) {
                log.info("------ Нет заявок. Проверь id версии");
                return;
            }
            versionName = redmineIssues.get(0).getTargetVersion().getName();
        } else {
            log.info("------ Берем заявки из редмайна по списку заявок {}", issueIds);
            redmineIssues = redmineConnector.getIssuesByIds(issueIds);
            versionName = versionId;
        }
        log.info("------ Взяли заявки из редмайна. Получено {} заявок.", redmineIssues.size());
        log.info("------ Берем прикрепленные файлы: функции, роли, константы.");
        List<Attachment> attachments = redmineConnector.getXmlAttachments(redmineIssues);

        //todo save to file on local folder
        log.info("------ Функции, роли, константы: {}", attachments);

        log.info("------ Берем данные о ревизиях из SVN");
        List<SvnObject> versionSvnObjects = new ArrayList<>();
        for (Issue issue : redmineIssues) {
            log.info("");
            log.info("Задача id={} Статус={} Назначена={} Тема={}", issue.getId(), issue.getStatusName(), issue.getAssigneeName(), issue.getSubject());
            List<Long> revisionIds = getRevisionIdsFromIssue(issue);
            List<SvnObject> issueSvnObjects = subversionConnector.readObjects(revisionIds);
            versionSvnObjects.addAll(issueSvnObjects);
        }
        log.info("------ Всего {} объектов собрано. Отбираем уникальные с последней ревизией по всем заявкам", versionSvnObjects.size());

        Collection<SvnObject> uniqueObjects = filterUniqueObjects(versionSvnObjects);
        log.info("");
        log.info("------ Уникальных объектов {}", uniqueObjects.size());
        log.info("");
        uniqueObjects.forEach(o -> log.info("{} {} {} {}", o.getRevisionDiff(), o.getLatestRevision(), o.getRevision(), o.getPath()));

        Path objFolder = createFolders(versionName);
        new ReadmeBuilder(redmineConnector).createReadmeFile(redmineIssues, versionName, objFolder);
        saveFiles(objFolder, subversionConnector, uniqueObjects, attachments);

        if (subversionConnector.isCopyJar()) {
            JarUtils.getJarFile(objFolder);
        }

        new PropertiesBuilder().buildProperties(versionName, objFolder);
        log.info("properties файл готов. Зипуем");
        new Zipper().zip(versionName, objFolder);
        new RtfBuilder().buildFromTemplate(versionName, objFolder);

    }


    public static void saveFiles(Path objFolder, SubversionConnector subversionConnector, Collection<SvnObject> uniqueObjects, List<Attachment> attachments) throws IOException, SVNException, RedmineException {
        subversionConnector.storeFiles(uniqueObjects, objFolder);
        log.info("------ Версия сохранена в {}", objFolder);
    }

    private static Path createFolders(String version) throws IOException {
        String versionFolder = mainFolder + "/" + version;
        String objFolder = versionFolder + "/obj";
        Files.createDirectory(Path.of(versionFolder));
        log.info("------ Сохраняем файлы в {}", objFolder);
        log.info("");
        return Files.createDirectory(Path.of(objFolder));
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
        return uniqueObjects.values();
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
