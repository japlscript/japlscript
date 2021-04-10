/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * TestTdta.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestTdta {

    @Test(expected = NullPointerException.class)
    public void testNullObjectReference() {
        new Tdta((String)null, "app");
    }

    @Test
    public void testGetSet() {
        final Tdta tdta = new Tdta("xxx", "app");
        tdta.setTdta(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, tdta.getTdta());
    }

    @Test
    public void testNewTdta() {
        final Tdta tdta = new Tdta(new byte[]{1, 2, 3, 4}, "app");
        assertArrayEquals(new byte[]{1, 2, 3, 4}, tdta.getTdta());
    }

    @Test
    public void testNewTdtaFromFile() throws IOException {
        final String filename = "picture.png";
        final File pictureFile = File.createTempFile("picture", filename);
        extractFile(filename, pictureFile);
        try {
            final Tdta tdta = new Tdta(pictureFile, "app");
            assertEquals(pictureFile.length(), tdta.getTdta().length);
        } finally {
            pictureFile.delete();
        }
    }

    private static void extractFile(final String filename, final File file) throws IOException {
        try (final InputStream in = Tdta.class.getResourceAsStream(filename);
             final OutputStream out = new FileOutputStream(file)) {
            final byte[] buf = new byte[1024*64];
            int justRead;
            while ((justRead = in.read(buf)) != -1) {
                out.write(buf, 0, justRead);
            }
        }
    }
}
