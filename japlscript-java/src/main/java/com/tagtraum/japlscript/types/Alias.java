/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Alias.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Alias extends ReferenceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(Alias.class);
    private final Path file;
    private String alias;

    private static final Alias instance = new Alias();

    private Alias() {
        super(null, null);
        file = null;
    }

    public Alias(final String objectReference, final String applicationReference) {
        super(toObjectReference(objectReference), applicationReference);
        final int startQuote = objectReference.indexOf('"');
        if (startQuote != -1) {
            final int endQuote = objectReference.lastIndexOf('"');
            this.alias = objectReference.substring(startQuote + 1, endQuote);
            this.file = Paths.get("/Volumes/" + alias.replace(':', '/'));
        } else {
            this.alias = objectReference; // TODO: this is not correct - right now it's real filename instead of alias
            this.file = Paths.get(objectReference);
        }
    }

    public static Alias getInstance() {
        return instance;
    }

    /**
     *
     * @param file java Path object
     * @throws IOException in case of IO issues
     */
    public Alias(final Path file) throws IOException {
        //super("alias \"" + com.tagtraum.japlscript.File.toApplescriptFile(file) + "\"", null);
        super(JaplScriptFile.toApplescriptFile(file), null);
        this.file = file;
    }

    /**
     *
     * @param file java File object
     * @throws IOException in case of IO issues
     */
    public Alias(final java.io.File file) throws IOException {
        this(file.toPath());
    }

    private static String toObjectReference(final String objectReference) {
        if (objectReference.startsWith("/")) {
            try {
                return JaplScriptFile.toApplescriptFile(new File(objectReference));
            } catch (IOException e) {
                LOG.error(e.toString(), e);
                return objectReference;
            }
        } else {
            return objectReference;
        }
    }

    public File getFile() {
        return this.file.toFile();
    }

    public Path getPath() {
        return this.file;
    }

    /**
     * Creates a URL for this Alias.
     *
     * @return the URL that corresponds to this Alias
     * @throws MalformedURLException if a valid URL cannot be formed
     */
    public URL getURL() throws MalformedURLException {
        // this does not look like it's perfect code...
        if (file.toString().startsWith("/")) {
            return new URL("file://localhost" + file);
        } else {
            return new URL("file://localhost/" + file);
        }
    }

    public String getAlias() {
        return this.alias;
    }

    @Override
    public String toString() {
        return getObjectReference();
    }

}
