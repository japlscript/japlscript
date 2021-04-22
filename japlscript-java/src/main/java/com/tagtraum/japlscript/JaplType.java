/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

/**
 * Type that is capable of both parsing and encoding AppleScript
 * objects (specifiers).
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface JaplType<T> {

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
    T _parse(String objectReference, String applicationReference);

    /**
     * Parse the given reference and create a corresponding
     * Java object for it. This may be a {@link Reference}
     * object itself, but may also be a primitive, e.g. a string
     * or a number.
     *
     * @param reference reference
     * @return either a {@link Reference} or a primitive
     */
    default T _parse(final Reference reference) {
        if (reference == null) return null;
        return _parse(reference.getObjectReference(), reference.getApplicationReference());
    }

    /**
     * Encode the given object as valid AppleScript string.
     *
     * @param object object
     * @return an AppleScript formatted string
     */
    String _encode(Object object);

    /**
     * The type used in Java interfaces that this {@link JaplType} corresponds to.
     * In case a primitive type is available (e.g. {@link Integer#TYPE}), that type
     * <em>should</em> be returned.
     *
     * @return class
     */
    Class<? extends T> _getInterfaceType();
}
