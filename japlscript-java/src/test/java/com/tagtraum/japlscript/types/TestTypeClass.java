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
 * TestTypeClass.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestTypeClass {

    @Test
    public void testBasics() {
        final TypeClass typeClass = new TypeClass("objRef", "appRef");
        assertNull(null, typeClass.getCode());
        assertNull(null, typeClass.getSuperClass());
        assertEquals("objRef", typeClass.getName());
        assertFalse("objRef", typeClass.isInstance("string"));
    }
}
