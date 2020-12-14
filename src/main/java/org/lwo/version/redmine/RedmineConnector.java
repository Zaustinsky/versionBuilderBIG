package org.lwo.version.redmine;

import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.Issue;
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


    public List<Issue> getChangesetsByIssueIds(Set<Integer> issuesIds) throws RedmineException {
        List<Issue> issues = getIssues(issuesIds);


        return issues;
        //.stream().flatMap(issue -> issue.getChangesets().stream()).collect(toSet());
    }

    private List<Issue> getIssues(Set<Integer> issuesIds) throws RedmineException {
        RedmineManager issueManager = getRedmineManager();
        List<Issue> issues = new ArrayList<>();
        for (Integer id : issuesIds) {
            Issue issueById = issueManager.getIssueManager().getIssueById(id, Include.changesets);
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
