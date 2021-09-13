/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

/**
 * Object that is capable of both decoding (parsing) and encoding AppleScript
 * objects (specifiers).
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see Reference
 */
public interface Codec<T> {

    /**
     * Parse the given reference and create a corresponding
     * Java object for it. This may be a {@link Reference}
     * object itself, but may also be a primitive, e.g. a string
     * or a number.
     *
     * @param objectReference object reference or primitive
     * @param applicationReference application reference (most likely
     *                             empty for primitives)
     * @return either a {@link Reference} or a primitive
     */
    T _decode(String objectReference, String applicationReference);

    /**
     * Parse the given reference and create a corresponding
     * Java object for it. This may be a {@link Reference}
     * object itself, but may also be a primitive, e.g. a string
     * or a number.
     *
     * @param reference reference
     * @return either a {@link Reference} or a primitive
     */
    default T _decode(final Reference reference) {
        if (reference == null) return null;
        return _decode(reference.getObjectReference(), reference.getApplicationReference());
    }

    /**
     * Encode the given object as valid AppleScript object reference.
     *
     * @param object object
     * @return an AppleScript formatted string
     */
    String _encode(Object object);

    /**
     * The type used in Java interfaces that this {@link Codec} corresponds to.
     * In case a primitive type is available (e.g. {@link Integer#TYPE}), that type
     * <em>should</em> be returned.
     *
     * @return class
     */
    Class<? extends T> _getJavaType();
}
