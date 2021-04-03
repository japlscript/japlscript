/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.junit.Test;

/**
 * TestTdta.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestTdta {

    @Test(expected = NullPointerException.class)
    public void testNullObjectReference() {
        new Tdta((String)null, "app");
    }
}
