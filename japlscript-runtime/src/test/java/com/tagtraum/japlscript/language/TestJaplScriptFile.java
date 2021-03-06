/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestJaplScriptFile.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJaplScriptFile {

    @Test
    public void testNullObjectReference() {
        Assertions.assertThrows(NullPointerException.class,
            () -> new JaplScriptFile(null, "app"));
    }

    @Test
    public void testExistingPath() throws IOException {
        final File f = File.createTempFile("pre", ".suf");
        try {
            final JaplScriptFile file = new JaplScriptFile(f.toPath());
            assertEquals(f, file.getFile());
            assertEquals(f.toPath(), file.getPath());
        } finally {
            f.delete();
        }
    }

    @Test
    public void testPOSIXFile() {
        final JaplScriptFile posixFile0 = new JaplScriptFile("POSIX file \"/Users/someone/Music\"", "app");
        assertEquals(Paths.get("/Users/someone/Music"), posixFile0.getPath());

        final JaplScriptFile posixFile1 = new JaplScriptFile("(POSIX file \"/Users/someone/Music\")", "app");
        assertEquals(Paths.get("/Users/someone/Music"), posixFile1.getPath());
    }

    @Test
    public void testAppleScriptFile() throws IOException {
        final File f = File.createTempFile("pre", ".suf");
        try {
            final String s = JaplScriptFile.toAppleScriptFile(f.toPath());
            assertEquals("(POSIX file (\"" + f.getCanonicalFile() + "\"))", s);
        } finally {
            f.delete();
        }
    }

    @Test
    public void testAlias() {
        final JaplScriptFile japlScriptFile = new JaplScriptFile("alias \"MacHD:Users:Something\"", "app");
        assertEquals(new File("/Volumes/MacHD/Users/Something"), japlScriptFile.getFile());
        assertEquals(Paths.get("/Volumes/MacHD/Users/Something"), japlScriptFile.getPath());
    }

    @Test
    public void testPlainFile() {
        final JaplScriptFile japlScriptFile = new JaplScriptFile("/someFile", "app");
        assertEquals(new File("/someFile"), japlScriptFile.getFile());
        assertEquals(Paths.get("/someFile"), japlScriptFile.getPath());
        assertEquals("/someFile", japlScriptFile.toString()); // is this really correct?
    }

}
