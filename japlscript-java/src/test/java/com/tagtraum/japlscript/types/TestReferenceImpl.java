/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TestReferenceImpl.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestReferenceImpl {

    @Test
    public void testBasics() {
        final ReferenceImpl ref0 = new ReferenceImpl("objRef", "appRef");
        assertEquals("objRef", ref0.getObjectReference());
        assertEquals("appRef", ref0.getApplicationReference());
        assertEquals("[appRef]: objRef", ref0.toString());
        final ReferenceImpl ref1 = new ReferenceImpl("objRef", "appRef");
        final ReferenceImpl ref2 = new ReferenceImpl("objRef2", "appRef2");
        assertTrue(ref0.equals(ref1));
        assertTrue(ref0.equals(ref0));
        assertFalse(ref0.equals(ref2));
        assertEquals("objRef".hashCode(), ref0.hashCode());
        assertEquals(0, new ReferenceImpl(null, null).hashCode());
    }
}
