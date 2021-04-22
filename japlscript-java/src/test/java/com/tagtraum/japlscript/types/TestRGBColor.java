/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplScriptException;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

/**
 * TestRGBColor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestRGBColor {

    @Test
    public void testParse() {
        final Color color = RGBColor.getInstance()._parse("{256, 256, 256}", null);
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

    @Test(expected = JaplScriptException.class)
    public void testParseBadRGBColor() {
        RGBColor.getInstance()._parse("1, 2", null);
    }

    @Test(expected = JaplScriptException.class)
    public void testParseBadRGBColor2() {
        RGBColor.getInstance()._parse("{1, 2", null);
    }


}
