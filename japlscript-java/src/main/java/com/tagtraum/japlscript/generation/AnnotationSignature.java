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
 * AnnotationSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class AnnotationSignature {
    private final Class<?> type;
    private final String[] arguments;

    public AnnotationSignature(final Class<?> type, final String... arguments) {
        Objects.requireNonNull(type, "type is mandatory");
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AnnotationSignature that = (AnnotationSignature) o;

        if (!type.equals(that.type)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    public String toString() {
        if (arguments == null || arguments.length == 0) return "@" + type.getName();
        return "@" + type.getName() + "(" + String.join(", ", arguments) + ")";
    }
}
