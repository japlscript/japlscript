/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import java.util.Objects;

import static com.tagtraum.japlscript.generation.JavadocSupport.toHTML;

/**
 * Field signature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class FieldSignature {

    private final String name;
    private final String description;

    public FieldSignature(final String name) {
        this(name, null);
    }

    public FieldSignature(final String name, final String description) {
        Objects.requireNonNull(name, "name is mandatory");
        this.name = name;
        this.description = description;
    }

    public String toJavadoc() {
        if (description == null) return null;
        return "/** " + toHTML(description) + " */";
    }

    @Override
    public String toString() {
        return name + ";";
    }
}
