package org.lwo.version.readme;

import org.lwo.version.svn.SvnObject;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JarAssambly {
    private SVNRepository repository;
    private final String url = "https://192.168.11.253";
    private final String name = "zaustinsky_d";
    private final String password = "Amenemhet1";


    static String mainFolder = "d:/versions/branches";

    public static void main(String[] args) throws SVNException, IOException {
        //Process c = Runtime.getRuntime().exec("cmd /c start " + " mvn clean");
        //Process p = Runtime.getRuntime().exec("cmd /c start " + " mvn compile -e");
        //Process rt = Runtime.getRuntime().exec("cmd /c start");

        //Process process1 = idea.sh<C:\Program Files\JetBrains\IntelliJ IDEA 2020.2.2\bin\idea64.exe>

        //Process process = Runtime.getRuntime().exec("cmd /c start " + " mvn clean compile package -e", null, new File("D:/SVN-BIG/branches/"));


        Process process = Runtime.getRuntime().exec("\"C:\\Program Files\\Java\\jdk1.8.0_261\\bin\\java.exe\" -Dmaven.multiModuleProjectDirectory=D:\\SVN-BIG\\branches \"-Dmaven.home=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\" \"-Dclassworlds.conf=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\bin\\m2.conf\" \"-Dmaven.ext.class.path=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven-event-listener.jar\" \"-javaagent:C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\lib\\idea_rt.jar=50945:C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\bin\" -Dfile.encoding=windows-1251 -classpath \"C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\boot\\plexus-classworlds-2.6.0.jar;C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\boot\\plexus-classworlds.license\" org.codehaus.classworlds.Launcher -Didea.version=2021.2 clean\n" , null, new File("D:/SVN-BIG/branches/"));
        process = Runtime.getRuntime().exec("\"C:\\Program Files\\Java\\jdk1.8.0_261\\bin\\java.exe\" -Dmaven.multiModuleProjectDirectory=D:\\SVN-BIG\\branches \"-Dmaven.home=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\" \"-Dclassworlds.conf=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\bin\\m2.conf\" \"-Dmaven.ext.class.path=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven-event-listener.jar\" \"-javaagent:C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\lib\\idea_rt.jar=52180:C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\bin\" -Dfile.encoding=windows-1251 -classpath \"C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\boot\\plexus-classworlds-2.6.0.jar;C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\boot\\plexus-classworlds.license\" org.codehaus.classworlds.Launcher -Didea.version=2021.2 compile\n" , null, new File("D:/SVN-BIG/branches/"));
        process = Runtime.getRuntime().exec("\"C:\\Program Files\\Java\\jdk1.8.0_261\\bin\\java.exe\" -Dmaven.multiModuleProjectDirectory=D:\\SVN-BIG\\branches \"-Dmaven.home=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\" \"-Dclassworlds.conf=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\bin\\m2.conf\" \"-Dmaven.ext.class.path=C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven-event-listener.jar\" \"-javaagent:C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\lib\\idea_rt.jar=56239:C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\bin\" -Dfile.encoding=windows-1251 -classpath \"C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\boot\\plexus-classworlds-2.6.0.jar;C:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.2.2\\plugins\\maven\\lib\\maven3\\boot\\plexus-classworlds.license\" org.codehaus.classworlds.Launcher -Didea.version=2021.2 package\n" , null, new File("D:/SVN-BIG/branches/"));

    }


    private static void saveBranches() throws SVNException, IOException {
        File parent = new File("d:/SVN-BIG/branches");
        File[] listOfFiles = parent.listFiles();
        Path destDir = Paths.get(mainFolder);

        if (listOfFiles != null && !parent.getName().isEmpty()) {
            for (File file : listOfFiles)
                Files.copy(file.toPath(), destDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);

            for (File file : parent.listFiles()) {
                if (parent.listFiles() != null && !file.getName().isEmpty()) {
                    Path copied = Paths.get(mainFolder);
                    Files.copy(file.toPath(), copied, StandardCopyOption.REPLACE_EXISTING);
                    //System.out.println(parent.list().length);

                    //File version = file.toPaths("d:/versions").getFileName().equals("2.303.255.1");
                }

            }


        }
    }
}