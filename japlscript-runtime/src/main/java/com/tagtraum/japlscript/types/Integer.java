/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.Codec;

/**
 * Integer.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Integer implements Codec<java.lang.Integer> {

    private static final Integer instance = new Integer();

    private Integer() {
    }

    public static Integer getInstance() {
        return instance;
    }


    @Override
    public java.lang.Integer _decode(final String objectReference, final String applicationReference) {
        return java.lang.Integer.valueOf(objectReference);
    }

    @Override
    public String _encode(final Object number) {
        return number == null
            ? "null" // or "missing value"?
            : number.toString();
    }

    @Override
    public Class<java.lang.Integer> _getJavaType() {
        return java.lang.Integer.TYPE;
    }
}
