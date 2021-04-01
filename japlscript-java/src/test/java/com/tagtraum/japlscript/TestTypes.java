/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * TestTypes.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestTypes {

    @Test
    public void testGetStandardJavaType() {
        assertEquals(Boolean.TYPE.getName(), Types.getStandardJavaType("boolean"));
        assertNull(Types.getStandardJavaType("XXX"));
    }

    @Test
    public void testToJavaConstant() {
        assertEquals("SOMEVALUE", Types.toJavaConstant("someValue"));
    }

    @Test
    public void testToCamelCaseClassName() {
        assertEquals("SomeValue", Types.toCamelCaseClassName("some_value"));
    }

    @Test
    public void testToCamelCaseMethodName() {
        assertEquals("someValue", Types.toCamelCaseMethodName("some_value"));
    }

    @Test
    public void testToJavaIdentifier() {
        assertEquals("some____$_value", Types.toJavaIdentifier("some\"!@#$_value"));
        assertEquals("_some____$_value", Types.toJavaIdentifier("!some\"!@#$_value"));
    }

}
