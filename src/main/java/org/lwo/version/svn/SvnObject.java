package org.lwo.version.svn;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SvnObject {
    String path;
    Long revision;
    Long latestRevision;
    Type type;

    public SvnObject(String path, Long revision, Long latestRevision) {
        this.path = path;
        this.revision = revision;
        this.latestRevision = latestRevision;
        this.type = parseType(path);
    }

    public Long getRevisionDiff() {
        return latestRevision - revision;
    }

    private Type parseType(String path) {
        if (path.startsWith("/Belinkasgroup/forms")) {
            return Type.FORM;
        }
        if (path.startsWith("/Belinkasgroup/reports")) {
            return Type.REPORT;
        }
        if (path.startsWith("/Belinkasgroup/sql")) {
            return Type.PACKAGE;
        }
        if (path.startsWith("/Belinkasgroup/SQL_script_before")) {
            return Type.SQL_BEFORE;
        }
        if (path.startsWith("/Belinkasgroup/doctypes")) {
            return Type.TYPE_DOC;
        }
        if (path.startsWith("/Belinkasgroup/acctypes")) {
            return Type.TYPE_ACC;
        }
        if (path.startsWith("/Belinkasgroup/consts")) {
            return Type.CONST;
        }
        if (path.startsWith("/Belinkasgroup/functions")) {
            return Type.FUNCTION;
        }
        if (path.startsWith("/Belinkasgroup/roles")) {
            return Type.ROLE;
        }
        if (path.startsWith("/Belinkasgroup/SQL_script_after")) {
            return Type.SQL_AFTER;
        }
        if (path.startsWith("/Belinkasgroup/sprav")) {
            return Type.SPRAV;
        }if (path.startsWith("/Belinkasgroup/api")) {
            return Type.API;
        }

        log.error("Unknown svn object type: {}" + path);
        return Type.UNKNOWN;
    }
}
