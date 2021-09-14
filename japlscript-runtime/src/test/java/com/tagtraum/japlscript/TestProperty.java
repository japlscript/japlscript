/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.TypeClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestProperty.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestProperty {

    @Test
    public void testBasics() {
        final Property property0 = new Property("code", "name", "javaName", String.class, TypeClass.getInstance("tName", "«property tCod»", null, null));

        assertEquals("code", property0.getCode());
        assertEquals("name", property0.getName());
        assertEquals("javaName", property0.getJavaName());
        assertEquals(String.class, property0.getJavaClass());
        assertEquals(TypeClass.getInstance("tName", "«property tCod»", null, null), property0.getTypeClass());

        final Property property1 = new Property("code", "name", "javaName", String.class, TypeClass.getInstance("tName", "«property tCod»", null, null));
        final Property property2 = new Property("othercode", "name", "javaName", String.class, TypeClass.getInstance("tName", "«property tCod»", null, null));
        final Property property3 = new Property("code", "othername", "javaName", String.class, TypeClass.getInstance("tName", "«property tCod»", null, null));
        final Property property4 = new Property("code", "name", "javaName", Boolean.class, TypeClass.getInstance("tName", "«property tCod»", null, null));
        final Property property5 = new Property("code", "name", "javaName", Boolean.class, TypeClass.getInstance("otherName", "«property tCod»", null, null));
        final Property property6 = new Property("code", "name", "otherJavaName", String.class, TypeClass.getInstance("tName", "«property tCod»", null, null));
        assertEquals(property0, property0);
        assertEquals(property0, property1);
        assertFalse(property0.equals(null));
        assertFalse(property0.equals("string"));
        assertNotEquals(property0, property2);
        assertNotEquals(property0, property3);
        assertNotEquals(property0, property4);
        assertNotEquals(property0, property5);
        assertNotEquals(property0, property6);
        assertEquals(property0.hashCode(), property1.hashCode());
        assertEquals(property0.toString(), property1.toString());
    }
}
