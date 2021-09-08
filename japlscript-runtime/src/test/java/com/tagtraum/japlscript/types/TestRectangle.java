/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.execution.JaplScriptException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals(new java.awt.Rectangle(1, 2, 2, 2), Rectangle.getInstance()._decode("{1, 2, 3, 4}", null));
        assertNull(Rectangle.getInstance()._decode(" ", null));
    }

    @Test
    public void testParseBadRectangle() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            Rectangle.getInstance()._decode("1, 2", null);
        });
    }

    @Test
    public void testParseBadRectangle2() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            Rectangle.getInstance()._decode("{1, 2", null);
        });
    }
}
