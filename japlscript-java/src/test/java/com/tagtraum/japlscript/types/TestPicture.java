/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * TestPicture.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestPicture {

    @Test(expected = NullPointerException.class)
    public void testNullObjectReference() {
        new Picture(null, "app");
    }

    @Test
    public void testGetSet() {
        final Picture picture = new Picture("xxx", "app");
        picture.setData(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, picture.getData());
    }

}
