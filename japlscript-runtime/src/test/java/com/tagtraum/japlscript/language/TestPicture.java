/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestPicture.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestPicture {

    @Test
    public void testNullObjectReference() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new Picture(null, "app");
        });
    }

    @Test
    public void testGetSet() {
        final Picture picture = new Picture("xxx", "app");
        picture.setData(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, picture.getData());
    }

    @Test
    public void testGetNullImage() throws IOException {
        final Picture picture = new Picture("xxx", "app");
        picture.setData(null);
        assertNull(picture.getImage());
    }

    @Test
    public void testGetImage() throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final InputStream in = Picture.class.getResourceAsStream("picture.png")) {
            final byte[] buf = new byte[1024*64];
            int justRead;
            while ((justRead = in.read(buf)) != -1) {
                bout.write(buf, 0, justRead);
            }
        }

        final Picture picture = new Picture("xxx", "app");
        picture.setData(bout.toByteArray());
        assertNotNull(picture.getImage());
        // TODO: Actually, we would like this to reflect the format...
        assertNull(picture.getFormat());
    }

}
