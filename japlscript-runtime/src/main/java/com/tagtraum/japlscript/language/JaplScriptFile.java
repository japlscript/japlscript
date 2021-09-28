/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.Chevron;
import com.tagtraum.japlscript.JaplScript;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JaplScript File.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class JaplScriptFile extends ReferenceImpl {

    private static final JaplScriptFile instance = new JaplScriptFile();
    private static final TypeClass[] CLASSES = {
        new TypeClass("file", new Chevron("class", "file"))
    };
    private final Path file;

    private JaplScriptFile() {
        super(null, null);
        this.file = null;
    }

    public JaplScriptFile(final String objectReference, final String applicationReference) {
        super(objectReference, applicationReference);
        final int startQuote = objectReference.indexOf('"');
        final int endQuote = objectReference.lastIndexOf('"');
        if (startQuote != -1 && endQuote != -1 && startQuote < endQuote && objectReference.contains(":")) {
            // we assume this is an alias
            final String alias = objectReference.substring(startQuote + 1, endQuote);
            this.file = Paths.get("/Volumes/" + alias.replace(':', '/'));
        } else if (startQuote != -1 && endQuote != -1 && startQuote < endQuote && objectReference.contains("(POSIX file \"")) {
            // we try to catch the "POSIX file" case
            this.file = Paths.get(objectReference.substring(startQuote + 1, endQuote));
        } else {
            // we assume this is a plain file name
            this.file = Paths.get(objectReference);
        }
    }

    public JaplScriptFile(final java.io.File file) throws IOException {
        this(file.toPath());
    }

    public JaplScriptFile(final java.nio.file.Path file) throws IOException {
        //super("file \"" + toAppleScriptFile(file) + "\"", null);
        super(toAppleScriptFile(file), null);
        this.file = file;
    }

    public static JaplScriptFile getInstance() {
        return instance;
    }

    /**
     *
     * @param javaFile java File object
     * @return POSIX file
     * @throws IOException if a canonical path cannot be found
     */
    public static String toAppleScriptFile(final java.io.File javaFile) throws IOException {
        return "(POSIX file " + JaplScript.quote(javaFile.getCanonicalFile().toString()) + ")";
    }

    /**
     *
     * @param javaPath java Path object
     * @return POSIX file
     * @throws IOException if a canonical path cannot be found
     */
    public static String toAppleScriptFile(final Path javaPath) throws IOException {
        return toAppleScriptFile(javaPath.toFile());
    }

    /**
     *
     * @return Java file this object represents. 
     */
    public java.io.File getFile() {
        return this.file.toFile();
    }

    /**
     *
     * @return Java file this object represents.
     */
    public Path getPath() {
        return this.file;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }

    @Override
    public String toString() {
        return getObjectReference();
    }

}
