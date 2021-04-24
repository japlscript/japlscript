/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import com.tagtraum.japlscript.Name;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TestAnnotationSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestAnnotationSignature {

    @Test
    public void testBasicsWithoutArguments() {
        final AnnotationSignature a0 = new AnnotationSignature(Name.class);
        final AnnotationSignature a1 = new AnnotationSignature(Name.class);
        final AnnotationSignature a2 = new AnnotationSignature(null);
        assertFalse(a0.equals(null));
        assertTrue(a0.equals(a0));
        assertTrue(a0.equals(a1));
        assertTrue(a0.hashCode() == a1.hashCode());
        assertTrue(a0.hashCode() != a2.hashCode());

        assertEquals("@" + Name.class.getName(), a0.toString());
    }

    @Test
    public void testBasicsWithArguments() {
        final AnnotationSignature a0 = new AnnotationSignature(Name.class, "hallo");
        final AnnotationSignature a1 = new AnnotationSignature(Name.class, "hallo");
        final AnnotationSignature a2 = new AnnotationSignature(null, "hallo");
        final AnnotationSignature a3 = new AnnotationSignature(null);
        assertFalse(a0.equals(null));
        assertTrue(a0.equals(a0));
        assertTrue(a0.equals(a1));
        assertTrue(a0.hashCode() == a1.hashCode());
        assertTrue(a0.hashCode() != a2.hashCode());
        assertTrue(a0.hashCode() != a3.hashCode());
        assertFalse(a0.equals(a3));

        assertEquals("@" + Name.class.getName() + "(hallo)", a0.toString());

    }
}
