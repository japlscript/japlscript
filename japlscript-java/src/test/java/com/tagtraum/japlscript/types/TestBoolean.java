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
 * TestBoolean.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestBoolean {

    @Test
    public void testEncode() {
        assertEquals("null", Boolean.getInstance()._encode(null));
        assertEquals("true", Boolean.getInstance()._encode("true"));
    }

}
