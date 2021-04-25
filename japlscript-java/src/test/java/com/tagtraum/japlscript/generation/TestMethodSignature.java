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
 * TestMethodSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestMethodSignature {

    @Test(expected = NullPointerException.class)
    public void testMandatoryName() {
        new MethodSignature(null);
    }

    public void testName() {
        final MethodSignature m = new MethodSignature("m");
        assertEquals("m", m.getName());
    }

    @Test
    public void testVisibility() {
        final MethodSignature m = new MethodSignature("m");
        assertNull(m.getVisibility());
        m.setVisibility("public");
        assertEquals("public", m.getVisibility());
        assertEquals("public m();", m.toString());
    }

    @Test
    public void testReturnType() {
        final MethodSignature m = new MethodSignature("m");
        assertNull(m.getReturnType());
        m.setReturnType("String");
        assertEquals("String", m.getReturnType());
        assertEquals("String m();", m.toString());
    }

    @Test
    public void testReturnTypeDescription() {
        final MethodSignature m = new MethodSignature("m");
        assertNull(m.getReturnTypeDescription());
        m.setReturnTypeDescription("Desc");
        assertEquals("Desc", m.getReturnTypeDescription());
        assertEquals("/**\n" +
            " *\n" +
            " * @return Desc\n" +
            " */", m.toJavadoc());
    }

    @Test
    public void testDescription() {
        final MethodSignature m = new MethodSignature("m");
        assertNull(m.getDescription());
        m.setDescription("Desc");
        assertEquals("Desc", m.getDescription());
        assertEquals("/**\n" +
            " * Desc\n" +
            " *\n" +
            " */", m.toJavadoc());
    }

    @Test
    public void testBody() {
        final MethodSignature m = new MethodSignature("m");
        assertNull(m.getBody());
        m.setBody("return null;");
        assertEquals("return null;", m.getBody());
        assertEquals("m() {\n" +
            "    return null;\n" +
            "}", m.toString());
    }

    @Test
    public void testDefault() {
        final MethodSignature m = new MethodSignature("m");
        assertFalse(m.isDefaultMethod());
        m.setDefaultMethod(true);
        assertTrue(m.isDefaultMethod());
        assertEquals("default m();", m.toString());
    }
}
