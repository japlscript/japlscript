/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.language.TypeClass;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Describes an AppleScript property at runtime.
 *
 * You may look up {@code Property} instances via their full name or via their
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

    /**
     * Main constructor.
     *
     * @param code AppleScript property code
     * @param name AppleScript property name
     * @param javaName Java property name
     * @param javaClass Java class of this property
     * @param typeClass AppleScript type for this property
     */
    public Property(final String code, final String name, final String javaName, final Class<?> javaClass, final TypeClass typeClass) {
        Objects.requireNonNull(code);
        Objects.requireNonNull(name);
        Objects.requireNonNull(javaName);
        Objects.requireNonNull(javaClass);
        Objects.requireNonNull(typeClass);
        this.code = code;
        this.name = name;
        this.javaName = javaName;
        this.javaClass = javaClass;
        this.typeClass = typeClass;
    }

    /**
     * Create instance based on a JaplScript annotated method.
     *
     * @param method method
     * @param application the main application class
     */
    public Property(final java.lang.reflect.Method method, final Class<?> application) {
        this(method.getAnnotation(Code.class).value(),
            method.getAnnotation(Name.class).value(),
            toJavaPropertyName(method),
            method.getReturnType(),
            new TypeClass(method.getAnnotation(Type.class).value(), null, application, null).intern());
    }

    /**
     * Convert the method to a valid Java property name.
     *
     * @param method method
     * @return valid Java property name
     */
    private static String toJavaPropertyName(final Method method) {
        final String methodName = method.getName();
        return methodName.startsWith("is")
            ? methodName.substring(2, 3).toLowerCase(Locale.ROOT) + methodName.substring(3)
            : methodName.substring(3, 4).toLowerCase(Locale.ROOT) + methodName.substring(4);
    }

    /**
     * Create a {@link Property} instances from the generated Java interfaces, based
     * on its JaplScript annotations.
     *
     * @param klass the interface/class to create property objects for
     * @param applicationInterface the main interface, typically an {@code Application} class.
     * @return set of properties
     */
    public static Set<Property> fromAnnotations(final Class<?> klass, final Class<?> applicationInterface) {
        return Arrays.stream(klass.getMethods())
            .filter(m -> !m.getReturnType().equals(Void.TYPE))
            .filter(m -> m.getAnnotation(Kind.class) != null && m.getAnnotation(Kind.class).value().equals("property"))
            // Disregard properties introduced by us when building
            // property set from annotations to avoid naming clashes.
            .filter(m -> !m.getDeclaringClass().equals(Reference.class))
            .map(method -> new Property(method, applicationInterface))
            .collect(Collectors.toSet());
    }

    /**
     * AppleScript property code.
     *
     * @return AppleScript property code
     */
    public String getCode() {
        return code;
    }

    /**
     * AppleScript property name.
     *
     * @return AppleScript property name
     */
    public String getName() {
        return name;
    }

    /**
     * Java property name.
     *
     * @return java property name
     */
    public String getJavaName() {
        return javaName;
    }

    /**
     * Java class of this property.
     *
     * @return java class
     */
    public Class<?> getJavaClass() {
        return javaClass;
    }

    /**
     * AppleScript type for this property.
     *
     * @return AppleScript type
     */
    public TypeClass getTypeClass() {
        return typeClass;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Property property = (Property) o;

        if (!code.equals(property.code)) return false;
        if (!name.equals(property.name)) return false;
        if (!javaName.equals(property.javaName)) return false;
        if (!javaClass.equals(property.javaClass)) return false;
        return typeClass.equals(property.typeClass);
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + javaName.hashCode();
        result = 31 * result + javaClass.hashCode();
        result = 31 * result + typeClass.hashCode();
        return result;
    }

    /**
     * Convert this property to chevron notation,
     * e.g. <code>«property size»</code>.
     *
     * @return chevron
     */
    public Chevron toChevron() {
        return new Chevron("property", code);
    }

    @Override
    public String toString() {
        return toChevron() + "[name=" + name
            + ",javaName=" + javaName
            + ",javaClass=" + javaClass.getName()
            + ",typeClass=" + typeClass + "]";
    }
}
