/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TestRectangle.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestRectangle {

    @Test
    public void testEncode() {
        assertEquals("{1, 2, 3, 4}", Rectangle.getInstance()._encode(new java.awt.Rectangle(1, 2, 2, 2)));
        assertEquals("null", Rectangle.getInstance()._encode(null));
    }

    @Test
    public void testParse() {
        assertEquals(new java.awt.Rectangle(1, 2, 2, 2), Rectangle.getInstance()._parse("{1, 2, 3, 4}", null));
    }

}
