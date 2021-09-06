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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TestAlias.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestAlias {

    @Test
    public void testNullReference() {
        assertNull(Alias.getInstance()._parse(null));
    }

    @Test
    public void testNullObjectReference() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new Alias(null, "app");
        });
    }

    @Test
    public void testAbsoluteFile() throws MalformedURLException {
        final Alias alias = new Alias("/SomeAbsoluteFile", "app");
        assertEquals(new File("/SomeAbsoluteFile"), alias.getFile());
        assertEquals(Paths.get("/SomeAbsoluteFile"), alias.getPath());
        assertEquals("(POSIX file (\"/SomeAbsoluteFile\"))", alias.toString());
        assertEquals(new URL("file://localhost/SomeAbsoluteFile"), alias.getURL());
    }

    @Test
    public void testRelativeFile() throws MalformedURLException {
        final Alias alias = new Alias("SomeRelativeFile", "app");
        assertEquals(new File("SomeRelativeFile"), alias.getFile());
        assertEquals(Paths.get("SomeRelativeFile"), alias.getPath());
        assertEquals(new URL("file://localhost/SomeRelativeFile"), alias.getURL());
    }

    @Test
    public void testAlias() {
        final Alias alias = new Alias("alias \"MacHD:Users:Something\"", "app");
        assertEquals(new File("/Volumes/MacHD/Users/Something"), alias.getFile());
        assertEquals(Paths.get("/Volumes/MacHD/Users/Something"), alias.getPath());
        assertEquals("MacHD:Users:Something", alias.getAlias());
    }

    @Test
    public void testNewAliasWithPath() throws IOException {
        final Alias alias = new Alias(Paths.get("/somefile"));
        assertEquals(new File("/somefile"), alias.getFile());
        assertEquals(Paths.get("/somefile"), alias.getPath());
    }

    @Test
    public void testNewAliasWithFile() throws IOException {
        final Alias alias = new Alias(new File("/somefile"));
        assertEquals(new File("/somefile"), alias.getFile());
        assertEquals(Paths.get("/somefile"), alias.getPath());
    }
}
