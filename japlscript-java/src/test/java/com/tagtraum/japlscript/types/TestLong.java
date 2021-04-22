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
 * TestLong.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestLong {

    @Test
    public void testEncode() {
        assertEquals("null", Long.getInstance()._encode(null));
        assertEquals("" + java.lang.Long.MAX_VALUE, Long.getInstance()._encode(java.lang.Long.MAX_VALUE));
    }

}
