/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.TypeClass;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@code Property} instance describes an AppleScript property at runtime.
 *
 * You may lookup {@code Property} instances via their full name or via their
 * AppleScript 4char code.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Property {

    private final String code;
    private final String name;
    private final String javaName;
    private final Class<?> javaClass;
    private final TypeClass typeClass;

    public Property(final String code, final String name, final String javaName, final Class<?> javaClass, final TypeClass typeClass) {
        this.code = code;
        this.name = name;
        this.javaName = javaName;
        this.javaClass = javaClass;
        this.typeClass = typeClass;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getJavaName() {
        return javaName;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public TypeClass getTypeClass() {
        return typeClass;
    }

    public Property(final java.lang.reflect.Method method) {
        this(method.getAnnotation(Code.class).value(),
            method.getAnnotation(Name.class).value(),
            method.getName().substring(3, 4).toLowerCase(Locale.ROOT) + method.getName().substring(4),
            method.getReturnType(),
            TypeClass.getInstance(method.getAnnotation(Type.class).value(), null, null, null));
    }

    public static Set<Property> fromAnnotations(final Class<?> klass) {
        return Arrays.stream(klass.getMethods())
            .filter(m -> !m.getReturnType().equals(Void.TYPE))
            .filter(m -> m.getAnnotation(Kind.class) != null && m.getAnnotation(Kind.class).value().equals("property"))
            .map(Property::new)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Property property = (Property) o;

        if (code != null ? !code.equals(property.code) : property.code != null) return false;
        if (name != null ? !name.equals(property.name) : property.name != null) return false;
        if (javaClass != null ? !javaClass.equals(property.javaClass) : property.javaClass != null) return false;
        return typeClass != null ? typeClass.equals(property.typeClass) : property.typeClass == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (javaClass != null ? javaClass.hashCode() : 0);
        result = 31 * result + (typeClass != null ? typeClass.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "«property " + code + "»[name=" + name
            + ",javaName=" + javaName
            + ",javaClass=" + javaClass.getName()
            + ",typeClass=" + typeClass + "]";
    }
}
