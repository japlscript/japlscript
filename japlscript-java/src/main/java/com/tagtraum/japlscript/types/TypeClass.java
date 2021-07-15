/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.Reference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@code TypeClass} instance describes an AppleScript class at runtime.
 * It lets you find out its superclass(es) and whether an instance is
 * an instance of a certain the type described by this class.
 *
 * You may lookup {@code TypeClass} instances via their full name or via their
 * AppleScript 4char code.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TypeClass extends ReferenceImpl {

    private static final Map<String, TypeClass> TYPE_CLASS_MAP = new HashMap<>();
    private static final TypeClass instance = new TypeClass();
    private String code;
    private TypeClass superClass;


    public TypeClass() {
        super(null, null);
        this.code = null;
        this.superClass = null;
    }

    /**
     *
     * @param objectReference object ref
     * @param applicationReference app ref
     */
    public TypeClass(final String objectReference, final String applicationReference) {
        super(lookupRefName(objectReference), applicationReference);
        if (objectReference.startsWith("\u00ab")) {
            this.code = objectReference;
        }
        synchronized (TypeClass.class) {
            final TypeClass typeClass = TYPE_CLASS_MAP.get(objectReference);
            if (typeClass != null) {
                this.superClass = typeClass.getSuperClass();
                this.code = typeClass.getCode();
            }
        }
    }

    public static TypeClass getInstance() {
        return instance;
    }

    private synchronized static String lookupRefName(final String name) {
        final TypeClass typeClass = TYPE_CLASS_MAP.get(name);
        return typeClass != null ? typeClass.getName() : name;
    }

    /**
     *
     * @param name e.g. user playlist
     * @param code e.g. cUsP
     * @param applicationReference app ref
     */
    private TypeClass(final String name, final String code, final String applicationReference, final TypeClass superClass) {
        super(name, applicationReference);
        this.code = code;
        this.superClass = superClass;
    }

    public synchronized static TypeClass getInstance(final String name, final String code, final String applicationReference, final TypeClass superClass) {
        // TODO: TypeClass lookup should be by application,
        //  because class names are not necessarily globally unique
        TypeClass typeClass = TYPE_CLASS_MAP.get(name);
        if (typeClass == null) {
            typeClass = new TypeClass(name, code, applicationReference, superClass);
            // why "applicationReference == null"? are we trying to work
            // around the globally uniqueness issue mentioned above?
            if (name != null && code != null && applicationReference == null) {
                TYPE_CLASS_MAP.put(name, typeClass);
                TYPE_CLASS_MAP.put(code, typeClass);
            }
        }
        return typeClass;
    }

    public TypeClass getSuperClass() {
        return superClass;
    }

    /**
     * @return name e.g. user playlist
     */
    public String getName() {
        return getObjectReference();
    }

    /**
     * @return code e.g. cUsP
     */
    public String getCode() {
        return code;
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
        if (getObjectReference().equals(code))
            return getObjectReference();
        else
            return getObjectReference() + "/" + code;
    }
}
