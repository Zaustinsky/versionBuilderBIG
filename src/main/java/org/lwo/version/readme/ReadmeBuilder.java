package org.lwo.version.readme;

import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Journal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwo.version.redmine.RedmineConnector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ReadmeBuilder {
    private final RedmineConnector redmineConnector;

    private final static String delimiter = "-----------------------------------";
    private static final String HEADER_FORMAT = """
            Версия %s от %s
            Сборщик версии: Заустинский Д.И.
                            
            В версии исполнены следующие заявки:
            """;
    private static final Set<String> internalProjects = Set.of("197* НДО БИГ"/*, "Белинкасгрупп: НДО - Эксплуатация"*/);

    public void createReadmeFile(List<Issue> redmineIssues, String version, Path objFolder) throws IOException {
        log.info("------ Генерация readme.txt");
        log.info("");

        List<String> comments = getAndFormatLastComment(redmineIssues);

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        final String header = String.format(HEADER_FORMAT, version, date);
        log.info("");

        StringBuilder builder = new StringBuilder(header);
        comments.forEach(builder::append);
        log.info("\n" + builder.toString());
        Path manualDirectory = Files.createDirectory(Path.of(objFolder + "/MANUAL"));
        Files.write(Path.of(manualDirectory + "/Read.LBRUS.txt"), builder.toString().getBytes());
        log.info("------ readme.txt готов");

    }

    public List<String> getAndFormatLastComment(List<Issue> redmineIssues) {
        return redmineIssues.stream().map(
                issue -> {

                        if (!isIncludeDescription(issue)) {
                            return "";
                        }
                    StringBuilder result = new StringBuilder();
                    result.append("\n\n\n")
                            .append(getFunctionalComplexName(issue))
                            .append(" ")
                            .append(String.format("(Заявка #%s)", issue.getId()))
                            .append("\n")
                            .append(delimiter)
                            .append("\n")
                            .append(getLastComment(issue))
                            .append("\n\n\n")
                            .append("Бизнес-аналитик: ")
                            .append(getBusinessAnalytic(issue));
                    return result.toString();
                }

        ).collect(Collectors.toList());
    }

    public Object getBusinessAnalytic(Issue issue) {
        if (internalProjects.contains(issue.getProjectName())) {
            return issue.getAuthorName();
        }

        CustomField businessAnalyst = issue.getCustomFieldByName("Бизнес-аналитик");
        if (businessAnalyst != null && businessAnalyst.getValue() != null && !businessAnalyst.getValue().isEmpty()) {
            return redmineConnector.getUserName(businessAnalyst.getValue());
        }

        return "нет";

    }


    public String getLastComment(Issue issue) {
        for (CustomField customField : issue.getCustomFields()) {
            System.out.println(customField);
            if ("Описание результата".equals(customField.getName())) {
                if (customField.getValue() != null && !customField.getValue().isEmpty()) {
                    return customField.getValue();
                }
            }
        }

        List<Journal> issueComments = issue.getJournals().stream()
                .filter(journal -> journal.getNotes() != null && !journal.getNotes().isBlank())
                .sorted(Comparator.comparing(journal -> journal.getCreatedOn().toInstant()))
                .collect(Collectors.toList());
        String lastComment;
        if (issueComments.isEmpty()) {
            lastComment = "";
        } else {
            lastComment = issueComments.get(issueComments.size() - 1).getNotes();
        }
        return lastComment;
    }

    public String getFunctionalComplexName(Issue issue) {
        return issue.getCustomFields().stream()
                .filter(field -> "ФК НДО".equals(field.getName()))
                .map(CustomField::getValue)
                .map(fk -> String.format("ФК \"%s\"", fk))
                .findFirst().orElse("ФК \"\"");
    }

    public Boolean isIncludeDescription(Issue issue) {
        for (CustomField customField : issue.getCustomFields()) {
            if ("Включение в описание".equals(customField.getName())) {
                if (customField.getValue() != null && !customField.getValue().isEmpty()) {
                    if ("Не включать".equals(customField.getValue())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}