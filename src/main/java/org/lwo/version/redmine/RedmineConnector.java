package org.lwo.version.redmine;

import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                .add("v[fixed_version_id][]", versionId);
        ResultsWrapper<Issue> issues = redmineManager.getIssueManager().getIssues(params);
        Set<Integer> issuesIds = issues.getResults().stream()
                .map(Issue::getId)
                .collect(toSet());
        return getIssues(issuesIds);
    }

    public List<Attachment> getXmlAttachments(List<Issue> issues) {
        List<Attachment> attachments = new ArrayList<>();
        for (Issue issue : issues) {
            for (Attachment attach: issue.getAttachments()) {
                String fileName = attach.getFileName();
                boolean endsWith = fileName.endsWith(".xml");
                if (endsWith == true) {
                    log.info(fileName);
                    attachments.add(attach);
                }

            }
        }
        return attachments;
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
