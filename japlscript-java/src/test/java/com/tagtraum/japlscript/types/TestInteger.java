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
 * TestInteger.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestInteger {

    @Test
    public void testEncode() {
        assertEquals("null", Integer.getInstance()._encode(null));
        assertEquals("1", Integer.getInstance()._encode((java.lang.Integer)1));
    }

}
