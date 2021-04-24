/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

/**
 * EnumSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class EnumSignature {

    private final String name;
    private final String[] arguments;

    public EnumSignature(final String name, final String... arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        if (arguments == null || arguments.length == 0) return name;
        return name + "(" + String.join(", ", arguments) + ")";
    }
}
