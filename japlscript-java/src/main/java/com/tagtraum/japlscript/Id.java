/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

/**
 * Id.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Id {
    private int value;

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
        return ((Integer) value).hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return ((Integer) value).equals(obj);
    }

    @Override
    public String toString() {
        return "id " + value;
    }
}
