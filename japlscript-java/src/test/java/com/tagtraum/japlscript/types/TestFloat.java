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
 * TestFloat.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestFloat {

    @Test
    public void testEncode() {
        assertEquals("null", Float.getInstance()._encode(null));
        assertEquals("1.0", Float.getInstance()._encode((java.lang.Float)1.0f));
    }

}
