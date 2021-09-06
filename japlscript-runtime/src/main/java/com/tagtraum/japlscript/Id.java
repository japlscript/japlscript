/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

/**
 * AppleScript object id.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Id {

    private final int value;

    /**
     * Creates an AppleScript id.
     *
     * @param value id
     */
    public Id(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Id) {
            return value == ((Id) obj).value;
        } else if (obj instanceof Integer) {
            // allow equality with Integer?
            return ((Integer) value).equals(obj);
        } else return false;
    }

    @Override
    public String toString() {
        return "id " + value;
    }
}
