/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        assertEquals("hallo", Text.getInstance()._parse("\"hallo\"", null));
        assertEquals("hallo", Text.getInstance()._parse(" \"hallo\" ", null));
        assertEquals("\"hallo", Text.getInstance()._parse(" \"hallo ", null));
        assertEquals("hallo\"", Text.getInstance()._parse(" hallo\" ", null));
        assertNull(Text.getInstance()._parse(null, null));
    }

}
