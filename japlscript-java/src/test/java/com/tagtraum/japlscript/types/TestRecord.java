/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TestRecord.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestRecord {

    @Test
    public void testBasics() {
        final Record record = new Record("objRef", "appRef");
        assertEquals("objRef", record.toString());
    }
}
