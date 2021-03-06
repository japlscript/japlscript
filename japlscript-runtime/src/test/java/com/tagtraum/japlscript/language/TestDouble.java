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
 * TestDouble.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDouble {

    @Test
    public void testEncode() {
        assertEquals("null", Double.getInstance()._encode(null));
        assertEquals("1.0", Double.getInstance()._encode((java.lang.Double)1.0));
    }

}
