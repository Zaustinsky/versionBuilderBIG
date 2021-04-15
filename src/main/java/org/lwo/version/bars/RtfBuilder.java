package org.lwo.version.bars;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RtfBuilder {

    public void buildFromTemplate(String versionName, Path folder) throws IOException {
        byte[] rtf = new byte[0];
        try {
            rtf = Files.readAllBytes(Path.of(ClassLoader.getSystemResource("protocol.rtf").toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //todo merge with PropertyBuilder
        List<File> files = Files.walk(folder, 2)
                .map(Path::toFile)
                .filter(file -> !file.isDirectory())
                .filter(file -> !"SPRAV".equals(file.getParentFile().getName()))
                .collect(Collectors.toList());

        var sb = new StringBuilder();
        var counter = 1;

        for (File file : files) {
            sb.append(counter++)
                    .append(". ")
                    .append(
                            switch (file.getParentFile().getName()) {
                                case "FORM" -> "Форма";
                                case "REPORT" -> "Отчет";
                                case "TYPE_DOC" -> "Тип документа";
                                case "PACKAGE" -> "Пакет";
                                case "FUNCTION" -> "Функции";
                                case "SQL_AFTER" -> "SQL-скрипт ПОСЛЕ установки версии";
                                case "MANUAL" -> "Документация";
                                case "JAR_FILE" -> "JAR-файл";
                                default -> "";
                            }
                    )
                    .append(": ")
                    .append(file.getParentFile().getName())
                    .append("\\\\")
                    .append(file.getName())
                    .append("\\line ");
        }

        String result = new String(rtf, "windows-1251")
                .replace("%%DATE%%", new SimpleDateFormat("dd.MM.yyyy").format(new Date()))
                .replace("%%VERSION%%", versionName)
                .replace("%%CONTENT%%", sb.toString());

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path file = Path.of(folder.toFile().getParent().concat("/LBRUS-" + versionName + "_" + date).concat(".rtf"));
        Files.createFile(file);
        Files.write(file, result.getBytes("windows-1251"), StandardOpenOption.WRITE);
    }
}
