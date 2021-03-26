/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test {@link Id}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestId {

    @Test
    public void testBasicId() {
        final Id id = new Id(1234);
        assertEquals(1234, id.getValue());
        assertEquals(Integer.hashCode(1234), id.hashCode());
        assertEquals("id 1234", id.toString());
    }
}
