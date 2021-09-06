/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplType;

/**
 * Float.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Float implements JaplType<java.lang.Float> {

    private static final Float instance = new Float();

    private Float() {
    }

    public static Float getInstance() {
        return instance;
    }


    @Override
    public java.lang.Float _parse(final String objectReference, final String applicationReference) {
        return java.lang.Float.valueOf(objectReference);
    }

    @Override
    public String _encode(final Object number) {
        return number == null
            ? "null" // or "missing value"?
            : number.toString();
    }

    @Override
    public Class<java.lang.Float> _getInterfaceType() {
        return java.lang.Float.TYPE;
    }
}
