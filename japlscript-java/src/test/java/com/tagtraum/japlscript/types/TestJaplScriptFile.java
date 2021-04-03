/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * TestJaplScriptFile.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJaplScriptFile {

    @Test(expected = NullPointerException.class)
    public void testNullObjectReference() {
        new JaplScriptFile(null, "app");
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

}