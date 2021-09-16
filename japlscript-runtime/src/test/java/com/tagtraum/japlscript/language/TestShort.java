/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestShort.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestShort {

    @Test
    public void testEncode() {
        assertEquals("null", Short.getInstance()._encode(null));
        assertEquals("1", Short.getInstance()._encode(java.lang.Short.valueOf("1")));
    }

}
