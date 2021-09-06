/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

import static com.tagtraum.japlscript.execution.NativeLibraryLoader.decodeURL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TestNativeLibraryLoader.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestNativeLibraryLoader {

    @Test
    public void testFindNonExistingFile() {
        Assertions.assertThrows(FileNotFoundException.class, () -> {
            NativeLibraryLoader.findFile("testFindFile", NativeLibraryLoader.class, pathname -> false);
        });
    }

    @Test
    public void testFindExistingFile() throws IOException {
        final File directory = NativeLibraryLoader.getClasspathOrJarDir(NativeLibraryLoader.class);
        final File tempFile = File.createTempFile("findFileTest", "lib", directory);
        tempFile.deleteOnExit();
        final String testFindFile = NativeLibraryLoader.findFile("testFindFile", NativeLibraryLoader.class, tempFile::equals);
        assertEquals(testFindFile, tempFile.toString());
    }

    @Test
    public void testLibFileFilter() throws IOException {
        final NativeLibraryLoader.LibFileFilter mylib = new NativeLibraryLoader.LibFileFilter("mylib");
        assertFalse(mylib.accept(new File("slnnfl")));
        assertFalse(mylib.accept(new File("mylib.dylib")));
        final File tempFile = File.createTempFile("mylib", ".dylib");
        tempFile.deleteOnExit();
        assertTrue(mylib.accept(tempFile));
    }

    @Test
    public void testDecodeURL() {
        assertEquals("someString", decodeURL("someString"));
        assertEquals("someString some", decodeURL("someString%20some"));
        assertEquals("  ", decodeURL("%20%20"));
    }

    @Test
    public void testDecodeURLIncompleteTrailingEscapePattern() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decodeURL("someString%h");
        });
    }

    @Test
    public void testDecodeURLIllegalHex() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decodeURL("someString%ah");
        });
    }

    @Test
    public void testDecodeURLNegativeValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decodeURL("someString%-1");
        });
    }
}
