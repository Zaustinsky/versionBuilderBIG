package org.lwo.version.svn;

import com.taskadapter.redmineapi.bean.Attachment;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.util.*;

@Slf4j

public class SubversionConnector {
    private final SVNRepository repository;
    private final String url = "https://192.168.11.253";
    private final String name = "zaustinsky_d";
    private final String password = "Amenemhet1";
    @Getter
    private boolean copyJar = false;

    public SubversionConnector() throws SVNException {
        repository = initRepository();
    }

    public List<SvnObject> readObjects(List<Long> revisions) throws SVNException {
        var svnObjects = new ArrayList<SvnObject>();
        long latestRevision = repository.getLatestRevision();

        for (Long revision : revisions) {
            Collection<SVNLogEntry> logEntries = repository.log(new String[]{""}, null, revision, revision, true, true);

            for (SVNLogEntry entry : logEntries) {
                for (Map.Entry<String, SVNLogEntryPath> e : entry.getChangedPaths().entrySet()) {
                    String key = e.getKey();
                    if (key.startsWith("/Belinkasgroup/branches")) {
                        copyJar = true;
                        continue;
                    }

                    if (key.startsWith("/Belinkasgroup/application")) continue;
                    if (key.startsWith("/Belinkasgroup/jar")) continue;
                    if (key.startsWith("/Belinkasgroup/.svn")) continue;
                    if (key.startsWith("/Belinkasgroup/web")) continue;
                    if (key.startsWith("/Belinkasgroup/Документация")) continue;
                    SvnObject svnObject = getSvnObject(latestRevision, revision, entry, key);
                    if (svnObject != null) {
                        svnObjects.add(svnObject);
                    }
                }
            }

        }

        return svnObjects;
    }

    public SvnObject getSvnObject(long latestRevision, Long revision, SVNLogEntry entry, String key) throws SVNException {
        try {
            Collection<SVNFileRevision> fileRevisions = repository.getFileRevisions(key, null, revision, latestRevision);
            Long latest = fileRevisions.stream()
                    .skip(fileRevisions.size() - 1)
                    .findFirst().map(SVNFileRevision::getRevision).orElse(revision);
            log.info("{} Ревизия->{} Последняя={} Объект={}", latest - revision, entry.getRevision(), latest, key);
            return new SvnObject(key, revision, latest);
        } catch (SVNException e) {
            if (e.getMessage() != null && e.getMessage().contains("is not a file in revision")) {
                log.warn("Ошибка получения объекта (удален?) Ревизии с {} по {} Объект={}", revision, latestRevision, key);
                return null;
            }
            log.error("Ошибка получения объекта Ревизии с {} по {} Объект={}", revision, latestRevision, key, e);
            throw e;
        }
    }

    public void storeFiles(Collection<SvnObject> objects, Path folder) throws SVNException, IOException {
        for (SvnObject object : objects) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            repository.getFile(object.path, -1L/*object.revision*/, null, baos);
            Files.write(getFilePath(folder, object), baos.toByteArray());
        }

    }

    public Path getFilePath(Path parentFolder, SvnObject object) throws IOException {
        String fileName = object.path.substring(1 + object.path.lastIndexOf("/"));
        Path path = Path.of(parentFolder + "/" + object.type);
        if (!path.toFile().exists()) {
            Files.createDirectory(path);
        }

        if (object.type == Type.SPRAV || object.type == Type.API) {
            return Path.of(parentFolder.getParent() + "/" + fileName);
        }

        return Path.of(parentFolder + "/" + object.type + "/" + fileName);
    }

    private SVNRepository initRepository() throws SVNException {
        DAVRepositoryFactory.setup();
        SVNRepository repository;
        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password.toCharArray());
        repository.setAuthenticationManager(authManager);
        return repository;
    }
}
