/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import com.tagtraum.japlscript.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestParameterSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestParameterSignature {

    @Test
    public void testBasics() {
        final ParameterSignature p0 = new ParameterSignature("s", "a string", "String", new AnnotationSignature(Parameter.class));
        final ParameterSignature p1 = new ParameterSignature("s", "a string", "String", new AnnotationSignature(Parameter.class));
        final ParameterSignature p2 = new ParameterSignature("s", "a string", "String");
        final ParameterSignature p3 = new ParameterSignature("s", null, "String", null);
        assertEquals("@com.tagtraum.japlscript.Parameter String s", p0.toString());
        assertEquals("String s", p3.toString());
        assertEquals(" * @param s a string", p0.toJavadoc());
        assertEquals(" * @param s", p3.toJavadoc());
        assertEquals(p0.hashCode(), p1.hashCode());
        assertTrue(p0.equals(p1));
        assertFalse(p0.equals("string"));
        assertFalse(p0.equals(p2));
        assertFalse(p0.equals(p3));
        assertFalse(p3.equals(p0));
        assertFalse(p0.equals(null));
        assertTrue(p0.equals(p0));
    }

    @Test
    public void testMandatoryName() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new ParameterSignature(null, "a string", "String");
        });
    }

    @Test
    public void testMandatoryType() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new ParameterSignature("name", "a string", null);
        });
    }
}
