package org.lwo.version.svn;

import org.lwo.version.Builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class JarUtils {
    public static void getJarFile(Path copied)  {
        File parent = new File("i:/BARS-Support/Версии НДО jar (Белинкассгрупп)");

        File[] files = parent.listFiles(); // каталоги версий

        File lastVersionFolder = files[files.length - 1];
        //System.out.println(Arrays.toString(parent.list()));

        File[] filesInVersion = lastVersionFolder.listFiles();
        File jar = filesInVersion[0];
        //System.out.println(Arrays.toString(lastVersionFolder.list()));

        try {
            Files.copy(jar.toPath(), Path.of(copied.toString()+"/bars_part_sign.jar"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Сохранен новый Jar");
        } catch (Exception e) {
            System.out.println("Не удалось сохранить Jar: "+e.getMessage());
        }

    }
}
