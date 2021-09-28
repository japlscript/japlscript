/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.*;

import java.util.Objects;

/**
 * Describes an AppleScript class at runtime.
 * It lets you find out its superclass(es) and whether an instance is
 * an instance of a certain the type described by this class.
 *
 * You may lookup {@code TypeClass} instances via their full name or via their
 * AppleScript 4char code.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TypeClass implements Reference, Codec<Reference> {

    private static final TypeClass instance = new TypeClass();
    private static final TypeClass[] CLASSES = {
        new TypeClass("class", new Chevron("class", "pcls")),
        new TypeClass("type", new Chevron("class", "type")),
        new TypeClass("type class", new Chevron("class", "type")),
    };
    private final String objectReference;
    private final String applicationReference;
    private String code;
    private TypeClass superClass;
    private Class<?> applicationInterface;
    private TypeClass typeClass;

    public TypeClass() {
        this(null, (String)null);
        this.code = null;
        this.superClass = null;
        this.applicationInterface = null;
    }

    /**
     *
     * @param objectReference object ref
     * @param applicationReference app ref
     */
    public TypeClass(final String objectReference, final String applicationReference) {
        this.objectReference = objectReference;
        this.applicationReference = applicationReference;
        if (objectReference != null && objectReference.startsWith("\u00ab")) {
            this.code = objectReference;
        }
    }

    /**
     *
     * @param name e.g. {@code user playlist}
     * @param code e.g. {@code «class cUsP»}
     */
    public TypeClass(final String name, final Chevron code) {
        this(name, code == null ? null : code.toString(), null, null, null);
    }
    /**
     *
     * @param name e.g. {@code user playlist}
     * @param code e.g. {@code «class cUsP»}
     * @param applicationInterface application class this type belongs to
     * @param superClass AppleScript super class
     */
    public TypeClass(final String name, final String code, final Class<?> applicationInterface, final TypeClass superClass) {
        this(name, code, null, applicationInterface, superClass);
    }

    /**
     *
     * @param name e.g. {@code user playlist}
     * @param code e.g. {@code «class cUsP»}
     * @param applicationReference application class this type belongs to
     * @param superClass AppleScript super class
     */
    public TypeClass(final String name, final String code, final String applicationReference, final TypeClass superClass) {
        this(name, code, applicationReference, null, superClass);
    }

    /**
     *
     * @param name e.g. {@code user playlist}
     * @param code e.g. {@code «class cUsP»}
     * @param applicationReference application class this type belongs to
     * @param applicationInterface application class this type belongs to
     * @param superClass AppleScript super class
     */
    public TypeClass(final String name, final String code, final String applicationReference, final Class<?> applicationInterface, final TypeClass superClass) {
        this(name, applicationReference);
        this.code = code;
        this.superClass = superClass;
        this.applicationInterface = applicationInterface;
    }

    @Override
    public String getObjectReference() {
        return objectReference;
    }

    @Override
    public String getApplicationReference() {
        return applicationReference;
    }

    public static TypeClass getInstance() {
        return instance;
    }

    /**
     * Read the class's <code>CLASS</code> field to determine the {@link TypeClass}
     * represented by this Java class/interface.
     *
     * @param klass klass
     * @return the TypeClass
     * @throws NoSuchFieldException if reflective access to the field {@code CLASS} fails
     * @throws IllegalAccessException if reflective access to the field {@code CLASS} fails
     */
    public static TypeClass fromClass(final Class<?> klass) throws NoSuchFieldException, IllegalAccessException {
        return (TypeClass)klass.getDeclaredField("CLASS").get(null);
    }

    public TypeClass getSuperClass() {
        return superClass;
    }

    /**
     * Return the main application class for the type represented by
     * this type class.
     * If this method returns null, you may want to call {@link #intern()}
     * to perform an application interface lookup based on a
     * set application reference.
     *
     * @return Java class or null, if not available.
     */
    public Class<?> getApplicationInterface() {
        return applicationInterface;
    }

    /**
     * @return name e.g. user playlist
     */
    public String getName() {
        return getObjectReference();
    }

    /**
     * Type code, with chevrons (e.g. {@code «class cUsP»}).
     *
     * @return chevron code e.g. {@code «class cUsP»}
     */
    public Chevron getCode() {
        return code == null ? null : Chevron.parse(code);
    }

    /**
     * Attempt to intern this instance of {@link TypeClass}.
     *
     * @return (if possible) interned version of this instance
     */
    public TypeClass intern() {
        return JaplScript.internTypeClass(this);
    }

    /**
     * Indicates whether the given class is assignable to this class.
     *
     * @param cls class
     * @return true, if this type class is assignable from the given class
     * @see Class#isAssignableFrom(Class)
     */
    public boolean isAssignableFrom(final TypeClass cls) {
        if (cls == null) return false;
        TypeClass otherClass = cls;
        while (otherClass != null && !otherClass.equals(this)) {
            otherClass = otherClass.getSuperClass();
        }
        return otherClass != null && otherClass.equals(this);
    }

    /**
     * Indicates whether the given object is an instance of this class.
     *
     * @param object object
     * @return true or false
     * @see Class#isInstance(Object)
     */
    public boolean isInstance(final Object object) {
        if (object == null) return false;
        if (!(object instanceof Reference)) return false;
        final Reference reference = (Reference)object;
        TypeClass thisClass = reference.getTypeClass();
        while (thisClass != null && !thisClass.equals(this)) {
            thisClass = thisClass.getSuperClass();
        }
        return thisClass != null && thisClass.equals(this);
    }

    @Override
    public int hashCode() {
        // because of weird equals logic, we accept bad hashcode distr.
        return 1;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TypeClass)) return false;
        final TypeClass that = (TypeClass)obj;
        if (this == that) return true;
        return Objects.equals(getObjectReference(), that.getObjectReference())
                || Objects.equals(getObjectReference(), that.code)
                || (this.code != null && this.code.equals(that.code))
                || (this.code != null && this.code.equals(that.getObjectReference()));
    }

    @Override
    public String toString() {
        if (getObjectReference() != null && getObjectReference().equals(code))
            return getObjectReference();
        else
            return getObjectReference() + "/" + code;
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
    public <T> T cast(final java.lang.Class<T> klass) {
        return JaplScript.cast(klass, this);
    }

    @Override
    public String _encode(final Object reference) {
        return ((Reference)reference).getObjectReference();
    }

    @Override
    public Class<? extends Reference> _getJavaType() {
        return TypeClass.class;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }

    @Override
    public TypeClass _decode(final String objectReference, final String applicationReference) {
        if (objectReference == null || "null".equals(objectReference) || "missing value".equals(objectReference)) return null;
        return new TypeClass(objectReference, applicationReference);
    }
}
