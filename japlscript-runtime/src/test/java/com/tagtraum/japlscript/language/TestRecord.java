/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestRecord.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestRecord {

    @Test
    public void testBasics() {
        final com.tagtraum.japlscript.language.Record record = new Record("objRef", "appRef");
        assertEquals("objRef", record.toString());
    }
}
