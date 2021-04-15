package org.lwo.version.bars;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PropertiesBuilder {

    public void buildProperties(String versionName, Path folder) throws IOException {

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        StringBuilder sb = new StringBuilder("PATCH_NUMBER=" + versionName + "\n" +
                                             "PATCH_TYPE=STANDART\n" +
                                             "WHOM_CREATE=Заустинский Дмитрий Иванович\n" +
                                             "WHEN_CREATE=" + df.format(new Date()) + "\n" +
                                             "DESCRIPTION=См. файлы Read.LBRUS.BIG\n" +
                                             "FULL_DESCRIPTION=См. файлы Read.LBRUS.BIG\n" +
                                             "#\n");
        var counter = 1;

        List<File> files = Files.walk(folder, 2)
                .map(Path::toFile)
                .filter(file -> !file.isDirectory())
                .filter(file -> !"SPRAV".equals(file.getParentFile().getName()))
                .collect(Collectors.toList());

        for (File file : files) {
            sb.append(counter++)
                    .append(". ")
                    .append(file.getParentFile().getName())
                    .append(": ")
                    .append(file.getName())
                    .append("\n");
        }


        Path propertiesFile = Path.of(folder + "/property-" + versionName + ".properties");
        Files.createFile(propertiesFile);
        Files.write(propertiesFile, sb.toString().getBytes(), StandardOpenOption.WRITE);


    }
}
