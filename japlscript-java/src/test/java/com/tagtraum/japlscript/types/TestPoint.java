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
 * TestPoint.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestPoint {

    @Test
    public void testEncode() {
        assertEquals("{1, 2}", Point.getInstance()._encode(new java.awt.Point(1, 2)));
        assertEquals("null", Point.getInstance()._encode(null));
    }

    @Test
    public void testParse() {
        assertEquals(new java.awt.Point(1, 2), Point.getInstance()._parse("{1, 2}", null));
    }

}
