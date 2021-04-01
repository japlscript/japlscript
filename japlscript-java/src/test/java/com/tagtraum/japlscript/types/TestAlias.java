/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * TestAlias.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestAlias {

    @Test(expected = NullPointerException.class)
    public void testNullObjectReference() {
        new Alias(null, "app");
    }

    @Test
    public void testSomeAbsoluteFile() {
        final Alias alias = new Alias("/SomeAbsoluteFile", "app");
        assertEquals(new File("/SomeAbsoluteFile"), alias.getFile());
        assertEquals(Paths.get("/SomeAbsoluteFile"), alias.getPath());
    }
}
