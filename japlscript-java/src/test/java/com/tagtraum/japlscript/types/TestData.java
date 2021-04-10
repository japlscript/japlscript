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
 * TestData.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestData {

    @Test(expected = NullPointerException.class)
    public void testNullObjectReference() {
        new Data(null, "app");
    }

    @Test
    public void testGetSet() {
        final Data data = new Data("xxx", "app");
        data.setData(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, data.getData());
    }

    @Test
    public void testParsing() {
        final Data data = new Data("«data ABCDBBBB»", "app");
        assertArrayEquals(new byte[]{-85, -51, -69, -69}, data.getData());
    }

}
