/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.Record;
import com.tagtraum.japlscript.types.TypeClass;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * TestGenerator.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestGenerator {

    @Test
    public void testAddTypeMapping() {
        final Generator generator = new Generator();

        final Generator.TypeMapping typeMapping = new Generator.TypeMapping();
        typeMapping.setApplescript("apple");
        typeMapping.setJava("java");

        assertEquals("java", typeMapping.getJava());
        assertEquals("apple", typeMapping.getApplescript());

        generator.addConfiguredTypeMapping(typeMapping);

        assertEquals("java", generator.getConfiguredTypeMapping("apple"));
        assertNull(generator.getConfiguredTypeMapping("xxx"));
    }

    @Test
    public void testExcludeClass() {
        final Generator generator = new Generator();

        final Generator.ExcludeClass excludeClass = new Generator.ExcludeClass();
        excludeClass.setName("apple");
        assertEquals("apple", excludeClass.getName());
        generator.addConfiguredExcludeClass(excludeClass);

        assertTrue(generator.isClassExcluded("apple"));
        assertFalse(generator.isClassExcluded("xxx"));
    }


    @Test
    public void testOut() {
        final Generator generator = new Generator();

        generator.setOut(Paths.get("out"));
        assertEquals(Paths.get("out"), generator.getOut());
        generator.setOut(new File("out"));
        assertEquals(Paths.get("out"), generator.getOut());
    }

    @Test
    public void testSdef() {
        final Generator generator = new Generator();

        generator.setSdef(Paths.get("aa.sdef"));
        assertEquals(Paths.get("aa.sdef"), generator.getSdef());
        generator.setSdef(new File("aa.sdef"));
        assertEquals(Paths.get("aa.sdef"), generator.getSdef());
    }

    @Test
    public void testPackagePrefix() {
        final Generator generator = new Generator();

        generator.setPackagePrefix("com.tagtraum");
        assertEquals("com.tagtraum", generator.getPackagePrefix());
        generator.setPackagePrefix("com.tagtraum.");
        assertEquals("com.tagtraum", generator.getPackagePrefix());
    }

    @Test
    public void testGenerateForMusic1_0_6_10() throws IOException, ClassNotFoundException {
        // copy resource to temp file
        generateForSdef("Music_1_0_6_10.sdef", "testGenerateForMusic1_0_6_10");
    }

    @Test
    public void testGenerateForFinder10_15_7() throws IOException, ClassNotFoundException {
        // copy resource to temp file
        generateForSdef("Finder_10_15_7.sdef", "testGenerateForFinder10_15_7");
    }

    @Test
    public void testGenerateForPhotos5_0() throws IOException, ClassNotFoundException {
        // copy resource to temp file
        generateForSdef("Photos_5_0.sdef", "testGenerateForPhotos5_0");
    }

    private void generateForSdef(final String filename, final String prefix) throws IOException, ClassNotFoundException {
        final File sdefFile = File.createTempFile(prefix, filename);
        final Path out = Files.createTempDirectory("generated");
        extractFile(filename, sdefFile);

        try {
            final Generator generator = new Generator();
            generator.setSdef(sdefFile);
            generator.setOut(out);
            generator.execute();

            // TODO: test some basics
            final String javaSourceFile = "com/tagtraum/japlscript/" + sdefFile.getName().replace(".sdef", "").toLowerCase() + "/Application.java";

            final URLClassLoader loader = compileGeneratedClasses(out);
            final String replace = javaSourceFile.replace(".java", "").replace('/', '.');
            System.out.println("Loading " + replace + " from " + out);
            loader.loadClass(replace);

        } finally {
            Files.walk(out)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    public void testCommands() throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        // copy resource to temp file
        final String filename = "Commands.sdef";
        final File sdefFile = File.createTempFile("Commands", filename);
        final Path out = Files.createTempDirectory("generated");
        extractFile(filename, sdefFile);

        try {
            final Generator generator = new Generator();
            generator.setSdef(sdefFile);
            generator.setOut(out);
            generator.execute();

            final String javaSourceFile = "com/tagtraum/japlscript/" + sdefFile.getName().replace(".sdef", "").toLowerCase() + "/Application.java";

            final URLClassLoader loader = compileGeneratedClasses(out);
            final String replace = javaSourceFile.replace(".java", "").replace('/', '.');
            System.out.println("Loading " + replace + " from " + out);
            final Class<?> applicationClass = loader.loadClass(replace);

            final Code appCode = applicationClass.getDeclaredAnnotation(Code.class);
            assertEquals("capp", appCode.value());
            final Name appName = applicationClass.getDeclaredAnnotation(Name.class);
            assertEquals("application", appName.value());

            final Method quitMethod = applicationClass.getDeclaredMethod("quit");
            assertEquals(Void.TYPE, quitMethod.getReturnType());
            final Kind quitKind = quitMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("command", quitKind.value());
            final Name quitName = quitMethod.getDeclaredAnnotation(Name.class);
            assertEquals("quit", quitName.value());
            final Parameter[] quitParameterAnnotations = getFirstParameterAnnotations(quitMethod);
            assertEquals(0, quitParameterAnnotations.length);

            final Method countMethod = applicationClass.getDeclaredMethod("count",
                Reference.class, Reference.class);
            assertEquals(Integer.TYPE, countMethod.getReturnType());
            final Kind countKind = countMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("command", countKind.value());
            final Name countName = countMethod.getDeclaredAnnotation(Name.class);
            assertEquals("count", countName.value());
            final Parameter[] countParameterAnnotations = getFirstParameterAnnotations(countMethod);
            assertNull(countParameterAnnotations[0]);
            assertEquals("each", countParameterAnnotations[1].value());

            final Method printMethod = applicationClass.getDeclaredMethod("print",
                Reference.class, Record.class);
            assertEquals(Void.TYPE, printMethod.getReturnType());
            final Kind printKind = printMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("command", printKind.value());
            final Name printName = printMethod.getDeclaredAnnotation(Name.class);
            assertEquals("print", printName.value());
            final Parameter[] printParameterAnnotations = getFirstParameterAnnotations(printMethod);
            assertNull(printParameterAnnotations[0]);
            assertEquals("with properties", printParameterAnnotations[1].value());

            final Method openMethod = applicationClass.getDeclaredMethod("open",
                Reference.class, Reference.class, Record.class);
            assertEquals(Void.TYPE, openMethod.getReturnType());
            final Kind openKind = openMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("command", openKind.value());
            final Name openName = openMethod.getDeclaredAnnotation(Name.class);
            assertEquals("open", openName.value());
            final Parameter[] openParameterAnnotations = getFirstParameterAnnotations(openMethod);
            assertNull(openParameterAnnotations[0]);
            assertEquals("using", openParameterAnnotations[1].value());
            assertEquals("with properties", openParameterAnnotations[2].value());

            final Field klass = applicationClass.getDeclaredField("CLASS");
            assertEquals(TypeClass.class, klass.getType());
            final TypeClass klassValue = (TypeClass)klass.get(null);
            assertEquals("«class capp»", klassValue.getCode());
            assertEquals("application", klassValue.getObjectReference());
            assertNull(klassValue.getApplicationReference());

        } finally {
            Files.walk(out)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }
    
    @Test
    public void testProperties() throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        // copy resource to temp file
        final String filename = "Properties.sdef";
        final File sdefFile = File.createTempFile("Properties", filename);
        final Path out = Files.createTempDirectory("generated");
        extractFile(filename, sdefFile);

        try {
            final Generator generator = new Generator();
            generator.setSdef(sdefFile);
            generator.setOut(out);
            generator.execute();

            final String javaSourceFile = "com/tagtraum/japlscript/" + sdefFile.getName().replace(".sdef", "").toLowerCase() + "/Application.java";

            final URLClassLoader loader = compileGeneratedClasses(out);
            final String replace = javaSourceFile.replace(".java", "").replace('/', '.');
            System.out.println("Loading " + replace + " from " + out);
            final Class<?> applicationClass = loader.loadClass(replace);

            final Code appCode = applicationClass.getDeclaredAnnotation(Code.class);
            assertEquals("capp", appCode.value());
            final Name appName = applicationClass.getDeclaredAnnotation(Name.class);
            assertEquals("application", appName.value());

            final Method getClipboardMethod = applicationClass.getDeclaredMethod("getClipboard");
            assertEquals(Reference.class, getClipboardMethod.getReturnType());
            final Kind getClipboardKind = getClipboardMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("property", getClipboardKind.value());
            final Name getClipboardName = getClipboardMethod.getDeclaredAnnotation(Name.class);
            assertEquals("clipboard", getClipboardName.value());
            final Code getClipboardCode = getClipboardMethod.getDeclaredAnnotation(Code.class);
            assertEquals("pcli", getClipboardCode.value());
            final Type getClipboardType = getClipboardMethod.getDeclaredAnnotation(Type.class);
            assertEquals("specifier", getClipboardType.value());

            final Method getNameMethod = applicationClass.getDeclaredMethod("getName");
            assertEquals(String.class, getNameMethod.getReturnType());
            final Kind getNameKind = getNameMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("property", getNameKind.value());
            final Name getNameName = getNameMethod.getDeclaredAnnotation(Name.class);
            assertEquals("name", getNameName.value());
            final Code getNameCode = getNameMethod.getDeclaredAnnotation(Code.class);
            assertEquals("pnam", getNameCode.value());
            final Type getNameType = getNameMethod.getDeclaredAnnotation(Type.class);
            assertEquals("text", getNameType.value());

            final Method getVisibleMethod = applicationClass.getDeclaredMethod("getVisible");
            assertEquals(Boolean.TYPE, getVisibleMethod.getReturnType());
            final Kind getVisibleKind = getVisibleMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("property", getVisibleKind.value());
            final Name getVisibleName = getVisibleMethod.getDeclaredAnnotation(Name.class);
            assertEquals("visible", getVisibleName.value());
            final Code getVisibleCode = getVisibleMethod.getDeclaredAnnotation(Code.class);
            assertEquals("pvis", getVisibleCode.value());
            final Type getVisibleType = getVisibleMethod.getDeclaredAnnotation(Type.class);
            assertEquals("boolean", getVisibleType.value());

            final Method setVisibleMethod = applicationClass.getDeclaredMethod("setVisible",
                Boolean.TYPE);
            assertEquals(Void.TYPE, setVisibleMethod.getReturnType());
            final Kind setVisibleKind = setVisibleMethod.getDeclaredAnnotation(Kind.class);
            assertEquals("property", setVisibleKind.value());
            final Name setVisibleName = setVisibleMethod.getDeclaredAnnotation(Name.class);
            assertEquals("visible", setVisibleName.value());
            final Code setVisibleCode = setVisibleMethod.getDeclaredAnnotation(Code.class);
            assertEquals("pvis", setVisibleCode.value());
            final Type setVisibleType = setVisibleMethod.getDeclaredAnnotation(Type.class);
            assertEquals("boolean", setVisibleType.value());

            final Field klass = applicationClass.getDeclaredField("CLASS");
            assertEquals(TypeClass.class, klass.getType());
            final TypeClass klassValue = (TypeClass)klass.get(null);
            assertEquals("«class capp»", klassValue.getCode());
            assertEquals("application", klassValue.getObjectReference());
            assertNull(klassValue.getApplicationReference());

        } finally {
            Files.walk(out)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    public void testElements() throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        // copy resource to temp file
        final String filename = "Elements.sdef";
        final File sdefFile = File.createTempFile("Elements", filename);
        final Path out = Files.createTempDirectory("generated");
        extractFile(filename, sdefFile);

        try {
            final Generator generator = new Generator();
            generator.setGenerateElementSetters(true);
            generator.setSdef(sdefFile);
            generator.setOut(out);
            generator.execute();

            final String packageFolderName = "com/tagtraum/japlscript/" + sdefFile.getName().replace(".sdef", "").toLowerCase();
            final String applicationSourceFile = packageFolderName + "/Application.java";
            final String fileSourceFile = packageFolderName + "/File.java";
            final String itemSourceFile = packageFolderName + "/Item.java";

            final URLClassLoader loader = compileGeneratedClasses(out);
            final String applicationClassName = applicationSourceFile.replace(".java", "").replace('/', '.');
            final String itemClassName = itemSourceFile.replace(".java", "").replace('/', '.');
            final String fileClassName = fileSourceFile.replace(".java", "").replace('/', '.');
            
            final Class<?> applicationClass = loader.loadClass(applicationClassName);
            final Class<?> itemClass = loader.loadClass(itemClassName);
            final Class<?> fileClass = loader.loadClass(fileClassName);

            // check inheritance
            assertTrue(itemClass.isAssignableFrom(fileClass));

            final Code appCode = applicationClass.getDeclaredAnnotation(Code.class);
            assertEquals("capp", appCode.value());
            final Name appName = applicationClass.getDeclaredAnnotation(Name.class);
            assertEquals("application", appName.value());

            final Method getItems = applicationClass.getDeclaredMethod("getItems");
            assertEquals(Array.newInstance(itemClass, 0).getClass(), getItems.getReturnType());
            final Kind getItemsKind = getItems.getDeclaredAnnotation(Kind.class);
            assertEquals("element", getItemsKind.value());
            final Type getItemsType = getItems.getDeclaredAnnotation(Type.class);
            assertEquals("item", getItemsType.value());

            final Method getItemsWithFilter = applicationClass.getDeclaredMethod("getItems", String.class);
            assertEquals(Array.newInstance(itemClass, 0).getClass(), getItemsWithFilter.getReturnType());
            final Kind getItemsWithFilterKind = getItemsWithFilter.getDeclaredAnnotation(Kind.class);
            assertEquals("element", getItemsWithFilterKind.value());
            final Type getItemsWithFilterType = getItemsWithFilter.getDeclaredAnnotation(Type.class);
            assertEquals("item", getItemsWithFilterType.value());

            final Method getItemWithIndex = applicationClass.getDeclaredMethod("getItem", Integer.TYPE);
            assertEquals(itemClass, getItemWithIndex.getReturnType());
            final Kind getItemWithIndexKind = getItemWithIndex.getDeclaredAnnotation(Kind.class);
            assertEquals("element", getItemWithIndexKind.value());
            final Type getItemWithIndexType = getItemWithIndex.getDeclaredAnnotation(Type.class);
            assertEquals("item", getItemWithIndexType.value());

            final Method getItemWithId = applicationClass.getDeclaredMethod("getItem", Id.class);
            assertEquals(itemClass, getItemWithId.getReturnType());
            final Kind getItemWithIdKind = getItemWithIndex.getDeclaredAnnotation(Kind.class);
            assertEquals("element", getItemWithIdKind.value());
            final Type getItemWithIdType = getItemWithIndex.getDeclaredAnnotation(Type.class);
            assertEquals("item", getItemWithIdType.value());


            final Method countItems = applicationClass.getDeclaredMethod("countItems");
            assertEquals(Integer.TYPE, countItems.getReturnType());
            final Kind countItemsKind = countItems.getDeclaredAnnotation(Kind.class);
            assertEquals("element", countItemsKind.value());
            final Type countItemsType = countItems.getDeclaredAnnotation(Type.class);
            assertEquals("item", countItemsType.value());

            final Method countItemsWithFilter = applicationClass.getDeclaredMethod("countItems", String.class);
            assertEquals(Integer.TYPE, countItemsWithFilter.getReturnType());
            final Kind countItemsWithFilterKind = countItemsWithFilter.getDeclaredAnnotation(Kind.class);
            assertEquals("element", countItemsWithFilterKind.value());
            final Type countItemsWithFilterType = countItemsWithFilter.getDeclaredAnnotation(Type.class);
            assertEquals("item", countItemsWithFilterType.value());

            final Method setItem = applicationClass.getDeclaredMethod("setItem", Integer.TYPE, itemClass);
            assertEquals(Void.TYPE, setItem.getReturnType());
            final Kind setItemKind = setItem.getDeclaredAnnotation(Kind.class);
            assertEquals("element", setItemKind.value());
            final Type setItemType = setItem.getDeclaredAnnotation(Type.class);
            assertEquals("item", setItemType.value());

            // TODO: add missing methods

            final Field klass = applicationClass.getDeclaredField("CLASS");
            assertEquals(TypeClass.class, klass.getType());
            final TypeClass klassValue = (TypeClass)klass.get(null);
            assertEquals("«class capp»", klassValue.getCode());
            assertEquals("application", klassValue.getObjectReference());
            assertNull(klassValue.getApplicationReference());

        } finally {
            Files.walk(out)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    public void testEnumerations() throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        // copy resource to temp file
        final String filename = "Enumerations.sdef";
        final File sdefFile = File.createTempFile("Enumerations", filename);
        final Path out = Files.createTempDirectory("generated");
        extractFile(filename, sdefFile);

        try {
            final Generator generator = new Generator();
            generator.setSdef(sdefFile);
            generator.setOut(out);
            generator.execute();

            final String packageFolderName = "com/tagtraum/japlscript/" + sdefFile.getName().replace(".sdef", "").toLowerCase();
            final String enumerationSourceFile = packageFolderName + "/Priv.java";

            final URLClassLoader loader = compileGeneratedClasses(out);
            final String enumerationClassName = enumerationSourceFile.replace(".java", "").replace('/', '.');

            final Class<?> enumerationClass = loader.loadClass(enumerationClassName);

            // check implements
            assertTrue(JaplEnum.class.isAssignableFrom(enumerationClass));

            final Code appCode = enumerationClass.getDeclaredAnnotation(Code.class);
            assertEquals("priv", appCode.value());
            final Name appName = enumerationClass.getDeclaredAnnotation(Name.class);
            assertEquals("priv", appName.value());

            final Object[] enumConstants = enumerationClass.getEnumConstants();

            assertEquals("READ_ONLY", enumConstants[0].toString());
            assertEquals("read", ((JaplEnum)enumConstants[0]).getCode());
            assertEquals("read only", ((JaplEnum)enumConstants[0]).getName());
            assertNull(((JaplEnum)enumConstants[0]).getDescription());

            assertEquals("READ_WRITE", enumConstants[1].toString());
            assertEquals("rdwr", ((JaplEnum)enumConstants[1]).getCode());
            assertEquals("read write", ((JaplEnum)enumConstants[1]).getName());
            assertNull(((JaplEnum)enumConstants[1]).getDescription());

            assertEquals("WRITE_ONLY", enumConstants[2].toString());
            assertEquals("writ", ((JaplEnum)enumConstants[2]).getCode());
            assertEquals("write only", ((JaplEnum)enumConstants[2]).getName());
            assertNull(((JaplEnum)enumConstants[2]).getDescription());
    
            assertEquals("NONE", enumConstants[3].toString());
            assertEquals("none", ((JaplEnum)enumConstants[3]).getCode());
            assertEquals("none", ((JaplEnum)enumConstants[3]).getName());
            assertEquals("none", ((JaplEnum)enumConstants[3]).getDescription());

            for (final Object o : enumConstants) {
                assertTrue(o instanceof JaplEnum);
            }

            final Method parse = enumerationClass.getDeclaredMethod("_parse", String.class, String.class);
            assertEquals(enumConstants[0], parse.invoke(enumConstants[0], "read", null));
            assertEquals(enumConstants[0], parse.invoke(enumConstants[0], "read only", null));
            assertEquals(enumConstants[0], parse.invoke(enumConstants[0], "«constant ****read»", null));

            // Do we need a type class?

        } finally {
            Files.walk(out)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    private static void extractFile(final String filename, final File file) throws IOException {
        try (final InputStream in = TestGenerator.class.getResourceAsStream(filename);
             final OutputStream out = new FileOutputStream(file)) {
            final byte[] buf = new byte[1024*64];
            int justRead;
            while ((justRead = in.read(buf)) != -1) {
                out.write(buf, 0, justRead);
            }
        }
    }

    /**
     * Compile generated source files and provide a suitable classloader to load them.
     *
     * @param out directory with java sources files, which will be walked recursively.
     * @return suitable classloader
     * @throws IOException if compilations fails
     */
    private static URLClassLoader compileGeneratedClasses(final Path out) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        final List<File> javaFiles = Files.walk(out).filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".java"))
            .map(Path::toFile)
            .collect(Collectors.toList());
        final Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjectsFromFiles(javaFiles);

        final StringWriter messages = new StringWriter();
        final Boolean res = compiler.getTask(messages, fileManager, null, null,
            null, compUnits).call();
        if (!res) throw new IOException("Failed to compile generated classes.\n" + messages.toString());

        final URL[] urls = {out.toUri().toURL()};
        return new URLClassLoader(urls, TestGenerator.class.getClassLoader());
    }

    private static Parameter[] getFirstParameterAnnotations(final Method method) {
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final Parameter[] parameters = new Parameter[parameterAnnotations.length];
        int j=0;
        for (final Annotation[] anns : parameterAnnotations) {
            if (anns.length > 0) parameters[j] = (Parameter) anns[0];
            j++;
        }
        return parameters;
    }

}
