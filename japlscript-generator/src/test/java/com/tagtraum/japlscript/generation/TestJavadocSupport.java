/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestJavadocSupport.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJavadocSupport {

    @Test
    public void testSpecialEntities() {
        assertEquals("&lt;&gt;&quot;<br/>&amp;&#169;", JavadocSupport.toHTML("<>\"\n&©"));
    }
}
