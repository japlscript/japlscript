/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestChevron.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestChevron {

    @Test
    public void testParseValid() {
        final Chevron chevron0 = Chevron.parse("«property size»");
        final Chevron chevron1 = Chevron.parse("«property size»");
        final Chevron chevron2 = Chevron.parse("«property dura»");
        final Chevron chevron3 = Chevron.parse("«class size»");
        final Chevron chevron4 = Chevron.parse("«class url »");
        assertEquals("property", chevron0.getKind());
        assertEquals("size", chevron0.getCode());
        assertEquals(chevron0, chevron0);
        assertEquals(chevron0, chevron1);
        assertNotEquals(chevron0, chevron2);
        assertNotEquals(chevron0, chevron3);
        assertEquals("«property size»", chevron0.toString());
        assertEquals(chevron0.hashCode(), chevron1.hashCode());
        assertFalse(chevron0.equals(null));
        assertFalse(chevron0.equals("string"));
    }

    @Test
    public void testParseWrongCodeLength() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Chevron.parse("«property 123»"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Chevron.parse("«property 12345»"));
    }

    @Test
    public void testSpaceInKind() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Chevron.parse("«pro perty size»"));
    }

    @Test
    public void testSpaceInKind2() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Chevron("kind ", "code"));
    }

    @Test
    public void testStartingWithChevron() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Chevron.parse("aa«property size»"));
    }

    @Test
    public void testEndingWithChevron() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Chevron.parse("«property size»aa"));
    }
}
