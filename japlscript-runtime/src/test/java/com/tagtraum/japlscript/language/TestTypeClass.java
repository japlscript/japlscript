/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.Chevron;
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
        final TypeClass typeClass0 = new TypeClass("objRef", "appRef");
        final TypeClass typeClass1 = new TypeClass(new Chevron("class", "text").toString(), "appRef");
        assertEquals(1, typeClass0.hashCode());
        assertNull(typeClass0.getCode());
        assertNull(typeClass0.getSuperClass());
        assertEquals("objRef", typeClass0.getName());
        assertFalse(typeClass0.isInstance("string"), "objRef");
        assertEquals("objRef/null", typeClass0.toString());

        assertNotEquals(typeClass0, typeClass1);
        assertEquals(new Chevron("class", "text"), typeClass1.getCode());
    }

    @Test
    public void testGetTypeClass() {
        final TypeClass typeClass0 = new TypeClass("text", Chevron.parse("«class text»"));
        final TypeClass typeClass1 = new TypeClass("text", Chevron.parse("«class text»"));
    }

    @Test
    public void testIsAssignableFrom() {
        final TypeClass typeClass0 = new TypeClass("text", Chevron.parse("«class text»"));
        final TypeClass typeClass1 = new TypeClass("text", Chevron.parse("«class text»"));
        assertFalse(typeClass0.isAssignableFrom(null));
        assertTrue(typeClass0.isAssignableFrom(typeClass1));
        assertTrue(typeClass1.isAssignableFrom(typeClass0));
    }

    @Test
    public void testIsInstanceNull() {
        final TypeClass typeClass0 = new TypeClass("text", Chevron.parse("«class text»"));
        assertFalse(typeClass0.isInstance(null));
    }

    @Test
    public void testIsAssignableFromWithSuperclass() {
        final TypeClass typeClass0 = new TypeClass("tex0", Chevron.parse("«class tex0»"));
        final TypeClass typeClass1 = new TypeClass("tex1", Chevron.parse("«class tex1»").toString(), (String)null, typeClass0);
        assertTrue(typeClass0.isAssignableFrom(typeClass1));
        assertFalse(typeClass1.isAssignableFrom(typeClass0));
    }
}
