package org.lwo.version.readme;

import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Journal;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ReadmeBuilder {
    private final static String delimiter = "-----------------------------------";
    private static final String HEADER_FORMAT = """
            Версия %s от %s
            Сборщик версии: Заустинский Д.И.
                            
            В версии исполнены следующие заявки:
            """;
    private static final Set<String> internalProjects = Set.of("197* НДО БИГ"/*, "Белинкасгрупп: НДО - Эксплуатация"*/);

    public void createReadmeFile(List<Issue> redmineIssues, String version, Path objFolder) throws IOException {
        log.info("------ Генерация readme.txt");

        List<String> comments = getAndFormatLastComment(redmineIssues);

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        final String header = String.format(HEADER_FORMAT, version, date);

        StringBuilder builder = new StringBuilder(header);
        comments.forEach(builder::append);
        log.info(builder.toString());
        Files.write(Path.of(objFolder.toString() + "/read.LBRUS.txt"), builder.toString().getBytes());
        log.info("------ readme.txt готов");

    }

    public List<String> getAndFormatLastComment(List<Issue> redmineIssues) {
        return redmineIssues.stream().map(
                issue -> {
                    StringBuilder result = new StringBuilder();
                    result.append("\n\n\n")
                            .append(getFunctionalComplexName(issue))
                            .append(" ")
                            .append(String.format("(Заявка #%s)", issue.getId()))
                            .append("\n")
                            .append(delimiter)
                            .append("\n")
                            .append(getLastComment(issue))
                            .append("\n")
                            .append("Бизнес-аналитик: ")
                            .append(internalProjects.contains(issue.getProjectName()) ? issue.getAuthorName() : "нет");
                    return result.toString();
                }
        ).collect(Collectors.toList());
    }

    public String getLastComment(Issue issue) {
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
}
