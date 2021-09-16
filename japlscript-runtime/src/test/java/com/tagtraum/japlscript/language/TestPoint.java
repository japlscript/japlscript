/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.execution.JaplScriptException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals(new java.awt.Point(1, 2), Point.getInstance()._decode("{1, 2}", null));
        assertNull(Point.getInstance()._decode(" ", null));
    }

    @Test
    public void testParseBadPoint() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            Point.getInstance()._decode("1, 2", null);
        });
    }

    @Test
    public void testParseBadPoint2() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            Point.getInstance()._decode("{1, 2", null);
        });
    }

}
