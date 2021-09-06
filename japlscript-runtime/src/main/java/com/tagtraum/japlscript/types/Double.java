/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplType;

/**
 * Double.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Double implements JaplType<java.lang.Double> {

    private static final Double instance = new Double();

    private Double() {
    }

    public static Double getInstance() {
        return instance;
    }


    @Override
    public java.lang.Double _parse(final String objectReference, final String applicationReference) {
        return java.lang.Double.valueOf(objectReference);
    }

    @Override
    public String _encode(final Object number) {
        return number == null
            ? "null" // or "missing value"?
            : number.toString();
    }

    @Override
    public Class<java.lang.Double> _getInterfaceType() {
        return java.lang.Double.TYPE;
    }
}