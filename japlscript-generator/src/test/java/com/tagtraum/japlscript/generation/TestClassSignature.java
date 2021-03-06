/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import com.tagtraum.japlscript.Name;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TestClassSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestClassSignature {

    @Test
    public void testBasicClass() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "Description.", null);
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " */\n" +
            "public class Name {\n" +
            "\n" +
            "}", classSignature.toString());

        assertNull(classSignature.getAuthor());
        assertEquals("Name", classSignature.getName());
        assertEquals("com.back", classSignature.getPackageName());
        assertEquals("Description.", classSignature.getDescription());
        assertEquals("class", classSignature.getType());
        assertEquals(new ArrayList<>(), classSignature.getMethodSignatures());
    }

    @Test
    public void testHTMLinDescription() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "> ©", null);
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * &gt; &#169;\n" +
            " */\n" +
            "public class Name {\n" +
            "\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testBasicClass2() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", null, null);
        assertEquals("package com.back;\n" +
            "\n" +
            "public class Name {\n" +
            "\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testAuthorNoDescription() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", null, "author");
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @author author\n" +
            " */\n" +
            "public class Name {\n" +
            "\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testAuthorAndDescription() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "Description.", "author");
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " *\n" +
            " * @author author\n" +
            " */\n" +
            "public class Name {\n" +
            "\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testImplements() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "Description.", null);
        classSignature.addImplements("Comparable");
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " */\n" +
            "public class Name implements Comparable {\n" +
            "\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testExtends() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "Description.", null);
        classSignature.addExtends("String");
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " */\n" +
            "public class Name extends String {\n" +
            "\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testClassAnnotation() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "Description.", null);
        classSignature.add(new AnnotationSignature(Name.class));
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " */\n" +
            "@com.tagtraum.japlscript.Name\n" +
            "public class Name {\n" +
            "\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testMethodSignature() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "Description.", null);
        final MethodSignature make = new MethodSignature("make");
        make.setReturnType("void");
        make.add(new ParameterSignature("s", "a string", "String"));
        classSignature.add(make);
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " */\n" +
            "public class Name {\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @param s a string\n" +
            " */\n" +
            "void make(String s);\n\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testFieldSignature() {
        final ClassSignature classSignature = new ClassSignature("class", "Name", "com.back", "Description.", null);
        classSignature.add(new FieldSignature("public static final String s = \"hallo\"", null));
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " */\n" +
            "public class Name {\n" +
            "\n" +
            "public static final String s = \"hallo\";\n\n" +
            "}", classSignature.toString());
    }

    @Test
    public void testEnumSignature() {
        final ClassSignature classSignature = new ClassSignature("enum", "Name", "com.back", "Description.", null);
        classSignature.add(new EnumSignature("READ", "\"one\"", "\"two\""));
        classSignature.add(new EnumSignature("WRITE", "\"one\"", "\"two\""));
        assertEquals("package com.back;\n" +
            "\n" +
            "/**\n" +
            " * Description.\n" +
            " */\n" +
            "public enum Name {\n" +
            "\n" +
            "    READ(\"one\", \"two\"),\n" +
            "    WRITE(\"one\", \"two\");\n\n" +
            "}", classSignature.toString());
    }
}
