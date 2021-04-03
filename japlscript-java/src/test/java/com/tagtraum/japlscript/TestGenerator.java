/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

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
    public void testGenerateForFinder10_15_7() throws IOException, ParserConfigurationException, SAXException {
        // copy resource to temp file
        final String filename = "Finder_10_15_7.sdef";
        final File sdefFile = File.createTempFile("testGenerateForFinder10_15_7", filename);
        final Path out = Files.createTempDirectory("generated");
        extractFile(filename, sdefFile);

        try {
            final Generator generator = new Generator();
            generator.setSdef(sdefFile);
            generator.setOut(out);
            generator.generate();

            // TODO: test some basics
            // TODO: compile generated classes

            // Compile source file.
//            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//            compiler.run()

        } finally {
            Files.walk(out)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    private void extractFile(final String filename, final File file) throws IOException {
        try (final InputStream in = getClass().getResourceAsStream(filename);
             final OutputStream out = new FileOutputStream(file)) {
            final byte[] buf = new byte[1024*64];
            int justRead;
            while ((justRead = in.read(buf)) != -1) {
                out.write(buf, 0, justRead);
            }
        }
    }

}
