package org.lwo.version.cli;

import com.taskadapter.redmineapi.RedmineException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.lwo.version.Builder;
import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public class Cli {
    public static void main(String[] args) throws SVNException, RedmineException, IOException {

        Options options = new Options();

        Option input = new Option("v", "version", true, "New version number");
        input.setRequired(true);

        options.addOption(input);

        Option output = new Option("i", "issue", true, "Comma separated issue ids");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.error(e.getMessage());
            formatter.printHelp("LWO Version Builder", options);
            System.exit(1);
        }

        String version = cmd.getOptionValue("version");
        String issueIdsString = cmd.getOptionValue("issue");

        Set<Integer> issueIds;
        if (issueIdsString == null || "".equals(issueIdsString)) {
            issueIds = Set.of();
        } else {
            issueIds = Arrays.stream(issueIdsString.split(",")).map(Integer::valueOf)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        log.info("Собираем версию {} по заявкам {}", version, issueIds);

        Builder.buildVersion(version, issueIds);
    }
}
