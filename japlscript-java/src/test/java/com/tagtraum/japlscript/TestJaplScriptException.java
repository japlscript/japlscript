/*
 * =================================================
 * Copyright 2015 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TestJaplScriptException.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJaplScriptException {

    @Test
    public void testUnicodeInMessage() {
        final JaplScriptException e = new JaplScriptException("A unknown token can\\U2019t go after this identifier.");
        assertEquals("A unknown token can\u2019t go after this identifier.", e.getError());
    }
}
