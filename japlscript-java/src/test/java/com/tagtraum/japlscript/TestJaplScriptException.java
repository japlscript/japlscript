/*
 * =================================================
 * Copyright 2015 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * TestJaplScriptException.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJaplScriptException {

    @Test
    public void testExceptionWithCause() {
        final RuntimeException e = new RuntimeException();
        final JaplScriptException exception = new JaplScriptException(e);
        assertEquals(exception.getCause(), e);
        assertEquals(e.toString(), exception.getError());
        assertNull(exception.getScript());
    }

    @Test
    public void testExceptionWithMessage() {
        final JaplScriptException exception = new JaplScriptException("message");
        assertNull(exception.getCause());
        assertEquals("message", exception.getError());
        assertNull(exception.getScript());
    }

    @Test
    public void testExceptionWithMessageAndCause() {
        final RuntimeException e = new RuntimeException();
        final JaplScriptException exception = new JaplScriptException("message", e);
        assertEquals(e, exception.getCause());
        assertEquals("message", exception.getError());
        assertNull(exception.getScript());
    }

    @Test
    public void testExceptionWithMessageAndScript() {
        final JaplScriptException exception = new JaplScriptException("message", "script");
        assertNull(exception.getCause());
        assertEquals("message", exception.getError());
        assertEquals("script", exception.getScript());
    }

    @Test
    public void testUnicodeInMessage() {
        final JaplScriptException e = new JaplScriptException("A unknown token can\\U2019t go after this identifier.");
        assertEquals("A unknown token can\u2019t go after this identifier.", e.getError());
    }
}
