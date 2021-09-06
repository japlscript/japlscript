/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    public void testMandatoryName() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new FieldSignature(null);
        });
    }
}
