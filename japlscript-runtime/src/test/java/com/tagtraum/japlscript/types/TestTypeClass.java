/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestTypeClass.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestTypeClass {

    @Test
    public void testBasics() {
        final TypeClass typeClass = new TypeClass("objRef", "appRef");
        assertEquals(1, typeClass.hashCode());
        assertNull(typeClass.getCode());
        assertNull(typeClass.getSuperClass());
        assertEquals("objRef", typeClass.getName());
        assertFalse(typeClass.isInstance("string"), "objRef");
        assertEquals("objRef/null", typeClass.toString());
    }

    @Test
    public void testGetTypeClass() {
        final TypeClass typeClass0 = TypeClass.getInstance("text", "text", null, null);
        final TypeClass typeClass1 = TypeClass.getInstance("text", "text", null, null);
    }

    @Test
    public void testIsAssignableFrom() {
        final TypeClass typeClass0 = TypeClass.getInstance("text", "text", null, null);
        final TypeClass typeClass1 = TypeClass.getInstance("text", "text", null, null);
        assertFalse(typeClass0.isAssignableFrom(null));
        assertTrue(typeClass0.isAssignableFrom(typeClass1));
        assertTrue(typeClass1.isAssignableFrom(typeClass0));
    }

    @Test
    public void testIsInstanceNull() {
        final TypeClass typeClass0 = TypeClass.getInstance("text", "text", null, null);
        assertFalse(typeClass0.isInstance(null));
    }

    @Test
    public void testIsAssignableFromWithSuperclass() {
        final TypeClass typeClass0 = TypeClass.getInstance("tex0", "tex0", null, null);
        final TypeClass typeClass1 = TypeClass.getInstance("tex1", "tex1", null, typeClass0);
        assertTrue(typeClass0.isAssignableFrom(typeClass1));
        assertFalse(typeClass1.isAssignableFrom(typeClass0));
    }
}
