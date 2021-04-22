/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

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
    public void testBasics() {
        final Color color = RGBColor.getInstance()._parse("{255, 255, 255}", null);
        assertEquals(new Color(1, 1, 1), color);
    }
}
