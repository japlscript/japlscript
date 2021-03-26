/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplScript;
import com.tagtraum.japlscript.ObjectInvocationHandler;
import com.tagtraum.japlscript.Reference;

/**
 * Immutable implementation of {@link com.tagtraum.japlscript.Reference}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ReferenceImpl implements Reference {

    private String objectReference;
    private String applicationReference;
    private TypeClass typeClass;

    /**
     * @param objectReference      object reference
     * @param applicationReference application reference
     */
    public ReferenceImpl(final String objectReference, final String applicationReference) {
        this.objectReference = objectReference;
        this.applicationReference = applicationReference;
    }

    @Override
    public String getObjectReference() {
        return objectReference;
    }

    @Override
    public String getApplicationReference() {
        return applicationReference;
    }

    @Override
    public synchronized TypeClass getTypeClass() {
        if (typeClass == null) {
            typeClass = new ObjectInvocationHandler(this).getTypeClass();
        }
        return typeClass;
    }

    @Override
    public boolean isInstanceOf(final TypeClass typeClass) {
        return typeClass != null && typeClass.isInstance(this);
    }

    @Override
    public String toString() {
        return "[" + applicationReference + "]: " + objectReference;
    }

    @Override
    public int hashCode() {
        if (objectReference == null) return 0;
        return objectReference.hashCode();
    }

    @Override
    public <T> T cast(final java.lang.Class<T> klass) {
        return JaplScript.cast(klass, this);
    }


}