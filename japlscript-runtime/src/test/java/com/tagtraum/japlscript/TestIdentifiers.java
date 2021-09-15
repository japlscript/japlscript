/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TestTypes.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestIdentifiers {

    @Test
    public void testGetStandardJavaType() {
        assertEquals(Boolean.TYPE.getName(), JaplScript.getStandardJavaType("boolean"));
        assertNull(JaplScript.getStandardJavaType("XXX"));
    }

    @Test
    public void testToJavaConstant() {
        assertEquals("SOMEVALUE", Identifiers.toJavaConstant("someValue"));
    }

    @Test
    public void testToCamelCaseClassName() {
        assertEquals("SomeValue", Identifiers.toCamelCaseClassName("some_value"));
    }

    @Test
    public void testToCamelCaseMethodName() {
        assertEquals("someValue", Identifiers.toCamelCaseMethodName("some_value"));
    }

    @Test
    public void testToJavaIdentifier() {
        assertEquals("some____$_value", Identifiers.toJavaIdentifier("some\"!@#$_value"));
        assertEquals("_some____$_value", Identifiers.toJavaIdentifier("!some\"!@#$_value"));
    }

}
