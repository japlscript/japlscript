/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

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

    private final Path file;

    public JaplScriptFile(final String objectReference, final String applicationReference) {
        super(objectReference, applicationReference);
        final int startQuote = objectReference.indexOf('"');
        final int endQuote = objectReference.lastIndexOf('"');
        if (startQuote != -1 && endQuote != -1 && startQuote < endQuote && objectReference.contains(":")) {
            // we assume this is an alias
            final String alias = objectReference.substring(startQuote + 1, endQuote);
            this.file = Paths.get("/Volumes/" + alias.replace(':', '/'));
        } else {
            // we assume this is a plain file name
            this.file = Paths.get(objectReference);
        }
    }

    public JaplScriptFile(final java.io.File file) throws IOException {
        this(file.toPath());
    }

    public JaplScriptFile(final java.nio.file.Path file) throws IOException {
        //super("file \"" + toApplescriptFile(file) + "\"", null);
        super(toApplescriptFile(file), null);
        this.file = file;
    }

    /**
     *
     * @param javaFile java File object
     * @return POSIX file
     * @throws IOException if a canonical path cannot be found
     */
    public static String toApplescriptFile(final java.io.File javaFile) throws IOException {
        return "(POSIX file " + JaplScript.quote(javaFile.getCanonicalFile().toString()) + ")";
    }

    /**
     *
     * @param javaPath java Path object
     * @return POSIX file
     * @throws IOException if a canonical path cannot be found
     */
    public static String toApplescriptFile(final Path javaPath) throws IOException {
        return toApplescriptFile(javaPath.toFile());
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
    public String toString() {
        return getObjectReference();
    }

}
