package org.lwo.version.svn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubversionConnectorTest {

    @Test
    void testFileName() {
        String path = "/BRIS/branches/BIG/forms/FORM-ServerScriptFunctionsLib.XML";
        String fileName = path.substring(1 + path.lastIndexOf("/"));
        assertEquals("FORM-ServerScriptFunctionsLib.XML", fileName);
    }

    @Test
    void testFileExtension() {
        String path = "/!svn/bc/287919/Belinkasgroup/branches/Gran/src/main/java/TAS/Client/NDO/cashControl/utils/NominalDataDialog.java";
        String extension = path.substring(1 + path.lastIndexOf("."));
        assertEquals("java", extension);
    }
}