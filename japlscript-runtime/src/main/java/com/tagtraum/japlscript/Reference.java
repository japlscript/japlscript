/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.TypeClass;

/**
 * Basic interface implemented by all JaplScript objects.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see JaplType
 */
public interface Reference {

    /**
     * Object reference.
     *
     * @return object reference
     */
    String getObjectReference();

    /**
     * Application reference.
     *
     * @return application reference
     */
    String getApplicationReference();

    /**
     * Cast this object to another applescript type.
     *
     * @param klass type to cast to
     * @param <T> target type
     * @return cast object
     */
    <T> T cast(java.lang.Class<T> klass);

    /**
     * Returns the AppleScript Class object for this object.
     *
     * @return class
     */
    @Type("type")
    @Name("class")
    @Code("type")
    @Kind("property")
    TypeClass getTypeClass();

    /**
     * Indicates whether this object is an instance of the given {@link TypeClass}.
     *
     * @param typeClass type class
     * @return true or false
     */
    boolean isInstanceOf(TypeClass typeClass);
}
