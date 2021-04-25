/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * TestFieldSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestFieldSignature {

    @Test
    public void testBasics() {
        final FieldSignature f0 = new FieldSignature("name", "description");
        assertEquals("name;", f0.toString());
        assertEquals("/** description */", f0.toJavadoc());
        final FieldSignature f1 = new FieldSignature("name");
        assertEquals("name;", f1.toString());
        assertNull(f1.toJavadoc());
    }

    @Test(expected = NullPointerException.class)
    public void testMandatoryName() {
        new FieldSignature(null);
    }
}
