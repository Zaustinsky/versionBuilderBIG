package org.lwo.version.svn;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SvnObject {
    String path;
    Long revision;
    Long latestRevision;

    public Long getRevisionDiff() {
        return latestRevision - revision;
    }
}
