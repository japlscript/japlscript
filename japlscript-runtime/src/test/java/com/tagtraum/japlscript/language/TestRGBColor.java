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

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestRGBColor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestRGBColor {

    @Test
    public void testParse() {
        final Color color = RGBColor.getInstance()._decode("{256, 256, 256}", null);
        assertEquals(new Color(1, 1, 1), color);
    }

    @Test
    public void testEncode() {
        assertEquals("{256, 256, 256}", RGBColor.getInstance()._encode(new java.awt.Color(1, 1, 1)));
        assertEquals("{0, 0, 0}", RGBColor.getInstance()._encode(new java.awt.Color(0, 0, 0)));
        // check this!
        assertEquals("{65534, 65534, 65534}", RGBColor.getInstance()._encode(new java.awt.Color(255, 255, 255)));
        assertEquals("null", RGBColor.getInstance()._encode(null));
    }

    @Test()
    public void testParseBadRGBColor() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            RGBColor.getInstance()._decode("1, 2", null);
        });
    }

    @Test()
    public void testParseBadRGBColor2() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            RGBColor.getInstance()._decode("{1, 2", null);
        });
    }


}
