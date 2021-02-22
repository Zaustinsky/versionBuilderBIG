package org.lwo.version.redmine;

import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lwo.version.Builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.util.stream.Collectors.toSet;

@Slf4j
public class RedmineConnector {

    String uri = "https://rdm.lwo.by";
    String login = "zaustinsky_d";
    String password = "Amenemhet1";
    String projectKey = "ndonkfo";
    Integer queryId = null; // any


    public List<Issue> getIssuesByIds(Set<Integer> issuesIds) throws RedmineException {
        return getIssues(issuesIds);
    }

    public List<Issue> getIssuesByVersion(String versionId) throws RedmineException {
        RedmineManager redmineManager = getRedmineManager();
        Params params = new Params()
                .add("set_filter", "1")
                .add("f[]", "fixed_version_id")
                .add("op[fixed_version_id]", "=")
                .add("v[fixed_version_id][]", versionId)
                .add("limit", "100");
        ResultsWrapper<Issue> issues = redmineManager.getIssueManager().getIssues(params);

        if (issues.getTotalFoundOnServer() > 100) {
            log.error("!!!!!!Внимание!!!!! Заявок в версии больше чем лимит (25) !!!!!");
            throw new RuntimeException();
        }

        Set<Integer> issuesIds = issues.getResults().stream()
                .map(Issue::getId)
                .collect(toSet());
        return getIssues(issuesIds);
    }

    public List<Attachment> getXmlAttachments(List<Issue> issues) {
        List<Attachment> attachments = new ArrayList<>();
        for (Issue issue : issues) {
            for (Attachment attach: issue.getAttachments()) {
                String fileName = attach.getFileName().toLowerCase();
                boolean endsWith = fileName.endsWith(".xml") || fileName.endsWith(".rar");
                if (endsWith == true) {
                    log.info(fileName);
                    log.info(String.valueOf(issue));
                    attachments.add(attach);
                }
            }
        }
        return attachments;
    }

    @SneakyThrows
    public String getUserName(String userId) {
        return getRedmineManager().getUserManager().getUserById(Integer.valueOf(userId)).getFullName();
    }

    public static void saveAttachments(List<Attachment> attachments, Path folder) throws RedmineException, IOException {
        for (Attachment object : attachments) {
            String fileName = object.getFileName();
            System.out.println((object) + "Ничего не вывелось" + fileName);
           // Files.copy(Path.of(URI.create(object.getContentURL())), Path.of("d:/versions/3998/obj/" + fileName), StandardCopyOption.REPLACE_EXISTING);
          //  Files.write(Path.of("d:/versions/3998/obj/" + fileName), Collections.singleton(object.getContentURL()));
        }
    }

    private List<Issue> getIssues(Set<Integer> issuesIds) throws RedmineException {
        RedmineManager issueManager = getRedmineManager();
        List<Issue> issues = new ArrayList<>();
        for (Integer id : issuesIds) {
            Issue issueById = issueManager.getIssueManager().getIssueById(id, Include.changesets,
                    Include.journals, Include.attachments);
            issues.add(issueById);
        }
        return issues;
    }

    private RedmineManager getRedmineManager() {
        RedmineManager mgr = RedmineManagerFactory.createWithUserAuth(uri, login, password);
        mgr.setObjectsPerPage(100);
        return mgr;
    }
}
