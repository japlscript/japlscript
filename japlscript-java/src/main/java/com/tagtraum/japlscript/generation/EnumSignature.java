/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import java.util.Arrays;
import java.util.Objects;

/**
 * EnumSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class EnumSignature {

    private final String name;
    private final String[] arguments;

    public EnumSignature(final String name, final String... arguments) {
        Objects.requireNonNull(name, "name is mandatory");
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final EnumSignature that = (EnumSignature) o;

        if (!name.equals(that.name)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public String toString() {
        if (arguments == null || arguments.length == 0) return name;
        return name + "(" + String.join(", ", arguments) + ")";
    }
}
