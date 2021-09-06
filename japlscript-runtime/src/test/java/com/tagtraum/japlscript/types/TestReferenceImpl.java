/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.execution.JaplScriptException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testNotInstanceOf() {
        final String applicationReference = "application \"Finder\"";
        final ReferenceImpl ref = new ReferenceImpl("\"hallo\"", applicationReference);
        assertFalse(ref.isInstanceOf(TypeClass.getInstance("some", "some", applicationReference, null)));
    }

    @Test
    public void testInstanceOf() {
        final String applicationReference = "application \"Finder\"";
        final ReferenceImpl ref = new ReferenceImpl("\"hallo\"", applicationReference);
        assertTrue(ref.isInstanceOf(TypeClass.getInstance("text", "«class ctxt»", applicationReference, null)));
        assertFalse(ref.isInstanceOf(null));
    }

    @Test
    public void testCast() {
        final String applicationReference = "application \"Finder\"";
        final ReferenceImpl ref = new ReferenceImpl("\"hallo\"", applicationReference);
        assertEquals("hallo", ref.cast(String.class));
    }

    @Test
    public void testBadParse() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            final ReferenceImpl ref = new ReferenceImplSubclass();
            ref._parse("", "");
        });
    }

    private static class ReferenceImplSubclass extends ReferenceImpl {
        public ReferenceImplSubclass(final String s, final String s1) throws Exception {
            super(s, s1);
            throw new Exception();
        }

        public ReferenceImplSubclass() {
            super(null, null);
        }
    }
}
