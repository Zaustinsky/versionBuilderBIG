package org.lwo.version.bars;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class Zipper {
    private static ZipOutputStream zos;

    public void zip(String versionName, Path folder) throws IOException {
        File file = folder.toFile();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String zipFileName = folder.toFile().getParent().concat("/LBRUS-" + versionName + "_" + date).concat(".ZIP");
        log.info("Zipping into " + zipFileName);
        zos = new ZipOutputStream(new FileOutputStream(zipFileName));
        Files.walkFileTree(folder, new ZipDir(folder));
        zos.close();
    }

    @RequiredArgsConstructor
    class ZipDir extends SimpleFileVisitor<Path> {
        private final Path sourceDir;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

            try {
                Path targetFile = sourceDir.relativize(file);
                zos.putNextEntry(new ZipEntry(targetFile.toString()));
                byte[] bytes = Files.readAllBytes(file);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();

            } catch (IOException ex) {
                System.out.println(ex);
            }

            return FileVisitResult.CONTINUE;
        }
    }

}

