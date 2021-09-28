/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.language.TypeClass;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestProperty.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestProperty {

    @Test
    public void testBasics() {
        final Property property0 = new Property("code", "name", "javaName", String.class, new TypeClass("tName", Chevron.parse("«property tCod»")));

        assertEquals("code", property0.getCode());
        assertEquals("name", property0.getName());
        assertEquals("javaName", property0.getJavaName());
        assertEquals(String.class, property0.getJavaClass());
        assertEquals(new TypeClass("tName", Chevron.parse("«property tCod»")), property0.getTypeClass());

        final Property property1 = new Property("code", "name", "javaName", String.class, new TypeClass("tName", Chevron.parse("«property tCod»")));
        final Property property2 = new Property("othercode", "name", "javaName", String.class, new TypeClass("tName", Chevron.parse("«property tCod»")));
        final Property property3 = new Property("code", "othername", "javaName", String.class, new TypeClass("tName", Chevron.parse("«property tCod»")));
        final Property property4 = new Property("code", "name", "javaName", Boolean.class, new TypeClass("tName", Chevron.parse("«property tCod»")));
        final Property property5 = new Property("code", "name", "javaName", Boolean.class, new TypeClass("otherName", Chevron.parse("«property tCod»")));
        final Property property6 = new Property("code", "name", "otherJavaName", String.class, new TypeClass("tName", Chevron.parse("«property tCod»")));
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

    @Test
    public void testFromMethod() throws NoSuchMethodException {
        final Method getComposer = TestClass.class.getDeclaredMethod("getComposer");
        final Method isCompilation = TestClass.class.getDeclaredMethod("isCompilation");
        final Property composer = new Property(getComposer, null);
        assertEquals("composer", composer.getJavaName());
        final Property compilation = new Property(isCompilation, null);
        assertEquals("compilation", compilation.getJavaName());
    }


    public interface TestClass {

        /**
         * Is this track from a compilation album?
         *
         * @return property value
         */
        @com.tagtraum.japlscript.Code("pAnt")
        @com.tagtraum.japlscript.Kind("property")
        @com.tagtraum.japlscript.Name("compilation")
        @com.tagtraum.japlscript.Type("boolean")
        boolean isCompilation();

        /**
         * The composer of the track.
         *
         * @return property value
         */
        @com.tagtraum.japlscript.Code("pCmp")
        @com.tagtraum.japlscript.Kind("property")
        @com.tagtraum.japlscript.Name("composer")
        @com.tagtraum.japlscript.Type("text")
        java.lang.String getComposer();
    }
}
