package org.lwo.version.bars;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Zipper {
    private static ZipArchiveOutputStream aos;

    public void zip(String versionName, Path folder) throws IOException {
        File file = folder.toFile();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String zipFileName = folder.toFile().getParent().concat("/LBRUS-" + versionName + "_" + date).concat(".ZIP");
        log.info("Zipping into " + zipFileName);
        aos = new ZipArchiveOutputStream(new File(zipFileName));
        aos.setEncoding("Cp866");
        aos.setUseLanguageEncodingFlag(false);
        aos.setFallbackToUTF8(false);
        Files.walkFileTree(folder, new ZipDir(folder));
        aos.finish();
    }

    @RequiredArgsConstructor
    class ZipDir extends SimpleFileVisitor<Path> {
        private final Path sourceDir;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

            try {
                Path targetFile = sourceDir.relativize(file);
                ArchiveEntry archiveEntry = aos.createArchiveEntry(targetFile.toFile(), targetFile.toString());
                aos.putArchiveEntry(archiveEntry);
                byte[] bytes = Files.readAllBytes(file);
                aos.write(bytes, 0, bytes.length);
                aos.closeArchiveEntry();

            } catch (IOException ex) {
                System.out.println(ex);
            }

            return FileVisitResult.CONTINUE;
        }
    }

}

