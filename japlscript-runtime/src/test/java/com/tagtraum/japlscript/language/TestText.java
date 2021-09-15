/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TestLong.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestText {

    @Test
    public void testEncode() {
        assertEquals("null", Text.getInstance()._encode(null));
        assertEquals("(\"hallo\")", Text.getInstance()._encode("hallo"));
    }

    @Test
    public void testParse() {
        assertEquals("hallo", Text.getInstance()._decode("\"hallo\"", null));
        assertEquals("hallo", Text.getInstance()._decode(" \"hallo\" ", null));
        assertEquals("\"hallo", Text.getInstance()._decode(" \"hallo ", null));
        assertEquals("hallo\"", Text.getInstance()._decode(" hallo\" ", null));
        assertNull(Text.getInstance()._decode(null, null));
    }

}
