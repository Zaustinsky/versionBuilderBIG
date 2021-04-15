package org.lwo.version.svn;

import org.lwo.version.Builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;

public class JarUtils {
    public static void getJarFile(Path copied) {
        File parent = new File("i:/BARS-Support/Версии НДО jar (Белинкассгрупп)");

        File[] files = parent.listFiles(); // каталоги версий
        File lastVersionFolder = files[files.length - 1];

        File[] filesInVersion = lastVersionFolder.listFiles();
        File jar = filesInVersion[0];

        try {
            Path jarFolder = Path.of(copied.toString() + "/JAR_FILE");
            Files.createDirectory(jarFolder);
            Files.copy(jar.toPath(), Path.of(jarFolder + "/bars_part_sign.jar"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Сохранен новый Jar");
        } catch (Exception e) {
            System.out.println("Не удалось сохранить Jar: " + e.getMessage());
        }

    }
}
