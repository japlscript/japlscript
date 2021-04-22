/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

/**
 * TestDate.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDate {

    @Test
    public void testEncode() {
        assertEquals("null", Date.getInstance()._encode(null));
        final java.util.Date date = new java.util.Date();
        assertEquals(new SimpleDateFormat("'my createDate('yyyy, M, d, H, m, s')'").format(date),
            Date.getInstance()._encode(date));
    }

}
