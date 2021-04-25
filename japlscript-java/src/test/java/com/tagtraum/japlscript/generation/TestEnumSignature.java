/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TestEnumSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestEnumSignature {

    @Test(expected = NullPointerException.class)
    public void testMandatoryName() {
        new EnumSignature(null);
    }

    @Test
    public void testBasics() {
        final EnumSignature e0 = new EnumSignature("E");
        final EnumSignature e1 = new EnumSignature("E");
        final EnumSignature e2 = new EnumSignature("Ea");
        final EnumSignature e3 = new EnumSignature("Ea", "args");
        final EnumSignature e4 = new EnumSignature("Ea", (String) null);
        assertEquals(e0.hashCode(), e1.hashCode());
        assertNotEquals(e0.hashCode(), e3.hashCode());
        assertTrue(e0.equals(e1));
        assertFalse(e0.equals(e2));
        assertFalse(e0.equals(null));
        assertFalse(e0.equals("string"));
        assertFalse(e4.equals(e3));
        assertFalse(e3.equals(e4));
        assertFalse(e0.equals(e3));
    }
}
