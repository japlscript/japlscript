/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

/**
 * FieldSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class FieldSignature {

    private final String name;
    private final String description;

    public FieldSignature(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String toJavadoc() {
        if (description == null) return null;
        return "/** " + description + " */";
    }

    @Override
    public String toString() {
        return name + ";";
    }
}
