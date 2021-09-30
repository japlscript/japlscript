/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.Chevron;
import com.tagtraum.japlscript.Codec;

/**
 * Integer.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Short implements Codec<java.lang.Short> {

    private static final Short instance = new Short();
    private static final TypeClass[] CLASSES = {
        new TypeClass("small integer", new Chevron("class", "shor"))
    };

    private Short() {
    }

    /**
     * Null instance used for {@link Codec} implementation.
     *
     * @return null instance
     */
    public static Short getInstance() {
        return instance;
    }

    @Override
    public java.lang.Short _decode(final String objectReference, final String applicationReference) {
        return java.lang.Short.valueOf(objectReference.trim());
    }

    @Override
    public String _encode(final Object number) {
        return number == null
            ? "null" // or "missing value"?
            : number.toString();
    }

    @Override
    public Class<java.lang.Short> _getJavaType() {
        return java.lang.Short.TYPE;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }
}
