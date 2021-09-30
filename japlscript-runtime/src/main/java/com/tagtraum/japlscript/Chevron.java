/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import java.util.Objects;

/**
 * Utility class to parse and create AppleScript codes like {@code «class pcls»}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Chevron {

    private final String kind;
    private final String code;

    public Chevron(final String kind, final String code) {
        if (kind.indexOf(' ') != -1) {
            throw new IllegalArgumentException("Chevron kind must not contain spaces: " + kind);
        }
        if (code.length() != 4) {
            throw new IllegalArgumentException("Chevron code must be four characters long: " + code);
        }
        this.kind = kind;
        this.code = code;
    }

    public static Chevron parse(final String chevronEncodedCode) {
        // be somewhat tolerant towards whitespace
        final String trimmedChevronEncodedCode = chevronEncodedCode.trim();
        if (!trimmedChevronEncodedCode.startsWith("«")) {
            throw new IllegalArgumentException("Chevron encoded code must start with \"«\", but does not: " + chevronEncodedCode);
        }
        if (!trimmedChevronEncodedCode.endsWith("»")) {
            throw new IllegalArgumentException("Chevron encoded code must end with \"»\", but does not: " + chevronEncodedCode);
        }
        final int space = trimmedChevronEncodedCode.indexOf(' ');
        final String kind = trimmedChevronEncodedCode.substring(1, space);
        final String code = trimmedChevronEncodedCode.substring(space + 1, trimmedChevronEncodedCode.length() - 1);
        return new Chevron(kind, code);
    }

    public String getKind() {
        return kind;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Chevron chevron = (Chevron) o;
        return kind.equals(chevron.kind) && code.equals(chevron.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, code);
    }

    @Override
    public String toString() {
        return "«" + kind + " " + code + "»";
    }
}
