package org.lwo.version.svn;

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
    private String url = "https://192.168.11.253";
    private String name = "zaustinsky_d";
    private String password = "Amenemhet1";

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
                    if (key.startsWith("/Belinkasgroup/branches")) continue;
                    SVNLogEntryPath path = e.getValue();
                    Collection<SVNFileRevision> fileRevisions = repository.getFileRevisions(key, null, revision, latestRevision);
                    Long latest = fileRevisions.stream().skip(fileRevisions.size() - 1).findFirst().map(SVNFileRevision::getRevision).orElse(revision);
                    log.info("{} Ревизия->{} Последняя={} Объект={}", latest-revision, entry.getRevision(), latest, key);
                    SvnObject svnObject = new SvnObject(key, revision, latest);
                    svnObjects.add(svnObject);
                }
            }
        }
        return svnObjects;
    }

    public void storeFiles(Collection<SvnObject> objects, Path folder) throws SVNException, IOException {
        for (SvnObject object: objects) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            repository.getFile(object.path, -1L, null, baos);
            Files.write(Path.of(folder + "/" + object.path.substring(1 + object.path.lastIndexOf("/"))), baos.toByteArray());
        }

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
