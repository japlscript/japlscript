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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * TestGenerator.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestGenerator {

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
    public void testGenerateForFinder10_15_7() throws IOException, ClassNotFoundException {
        // copy resource to temp file
        final String filename = "Finder_10_15_7.sdef";
        final File sdefFile = File.createTempFile("testGenerateForFinder10_15_7", filename);
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
