/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

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
        Assertions.assertThrows(NullPointerException.class, () -> {
            new JaplScriptFile(null, "app");
        });
    }

    @Test
    public void testExistingPath() throws IOException {
        final File f = File.createTempFile("pre", ".suf");
        try {
            final JaplScriptFile file = new JaplScriptFile(f);
            assertEquals(f, file.getFile());
            assertEquals(f.toPath(), file.getPath());
        } finally {
            f.delete();
        }
    }

    @Test
    public void testApplescriptFile() throws IOException {
        final File f = File.createTempFile("pre", ".suf");
        try {
            final String s = JaplScriptFile.toApplescriptFile(f);
            assertEquals("(POSIX file (\"" + f.getCanonicalFile().toString() + "\"))", s);
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
