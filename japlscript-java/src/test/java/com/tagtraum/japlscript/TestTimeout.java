/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TestTimeout.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestTimeout {

    @Test
    public void testBefore() {
        final Timeout timeout5 = new Timeout(5);
        assertEquals("with timeout of 5 seconds", timeout5.before(null, null));
        final Timeout timeout1 = new Timeout(1);
        assertEquals("with timeout of 1 second", timeout1.before(null, null));
    }

    @Test
    public void testAfter() {
        final Timeout timeout5 = new Timeout(5);
        assertEquals("end timeout", timeout5.after(null, null));
    }

    @Test
    public void testBasics() {
        assertNotNull(new Timeout(5).toString());
        assertEquals(5, new Timeout(5).hashCode());
        final Timeout timeout = new Timeout(5);
        assertTrue(timeout.equals(timeout));
        assertTrue(timeout.equals(new Timeout(5)));
        assertFalse(new Timeout(6).equals(new Timeout(5)));
        assertFalse(new Timeout(5).equals(new Object()));
        assertFalse(new Timeout(5).equals(null));
    }
}
