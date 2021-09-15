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
