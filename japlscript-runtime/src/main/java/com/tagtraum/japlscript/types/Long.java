/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplType;

/**
 * Long.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Long implements JaplType<java.lang.Long> {

    private static final Long instance = new Long();

    private Long() {
    }

    public static Long getInstance() {
        return instance;
    }


    @Override
    public java.lang.Long _parse(final String objectReference, final String applicationReference) {
        return java.lang.Long.valueOf(objectReference);
    }

    @Override
    public String _encode(final Object number) {
        return number == null
            ? "null" // or "missing value"?
            : number.toString();
    }

    @Override
    public Class<java.lang.Long> _getInterfaceType() {
        return java.lang.Long.TYPE;
    }
}
