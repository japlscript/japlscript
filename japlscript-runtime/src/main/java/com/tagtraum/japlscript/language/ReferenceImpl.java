/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.*;
import com.tagtraum.japlscript.execution.JaplScriptException;

/**
 * Immutable implementation of {@link com.tagtraum.japlscript.Reference}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ReferenceImpl implements Reference, Codec<Reference> {

    private static final ReferenceImpl instance = new ReferenceImpl();
    private static final TypeClass[] CLASSES = {
        new TypeClass("reference", new Chevron("class", "obj ")),
        new TypeClass("specifier", new Chevron("class", "obj ")),
        new TypeClass("anything", new Chevron("class", "****")),
        new TypeClass("any", new Chevron("class", "****"))
    };
    private final String objectReference;
    private final String applicationReference;
    private TypeClass typeClass;

    private ReferenceImpl() {
        this.objectReference = null;
        this.applicationReference = null;
    }

    /**
     * Null instance used for {@link Codec} implementation.
     *
     * @return null instance
     */
    public static ReferenceImpl getInstance() {
        return instance;
    }

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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceImpl)) return false;

        final ReferenceImpl reference = (ReferenceImpl) o;

        if (objectReference != null ? !objectReference.equals(reference.objectReference) : reference.objectReference != null)
            return false;
        return applicationReference != null ? applicationReference.equals(reference.applicationReference) : reference.applicationReference == null;
    }

    @Override
    public <T> T cast(final java.lang.Class<T> klass) {
        return JaplScript.cast(klass, this);
    }

    @Override
    public String _encode(final Object reference) {
        return ((Reference)reference).getObjectReference();
    }

    @Override
    public Class<? extends Reference> _getJavaType() {
        return getClass() == ReferenceImpl.class
            ? Reference.class
            : getClass();
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }

    @Override
    public ReferenceImpl _decode(final String objectReference, final String applicationReference) {
        if (objectReference == null || "".equals(objectReference) || "null".equals(objectReference) || "missing value".equals(objectReference)) return null;
        try {
            return this.getClass()
                .getConstructor(String.class, String.class)
                .newInstance(objectReference, applicationReference);
        } catch (Exception e) {
            throw new JaplScriptException("Failed to create new ReferenceImpl for " + this.getClass(), e);
        }
    }
}
