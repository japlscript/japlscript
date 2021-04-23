/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ClassSignature {

    private final String packageName;
    private final String description;
    private final String name;
    private final String type;
    private final List<String> extendedClasses = new ArrayList<>();
    private final List<String> implementedClasses = new ArrayList<>();
    private final List<AnnotationSignature> annotationSignatures = new ArrayList<>();
    private final List<MethodSignature> methodSignatures = new ArrayList<>();
    private final List<FieldSignature> fieldSignatures = new ArrayList<>();

    public ClassSignature(final String type, final String name, final String packageName, final String description) {
        this.packageName = packageName;
        this.description = description;
        this.name = name;
        this.type = type;
    }

    public String getFullyQualifiedClassName() {
        return packageName + "." + name;
    }

    public void add(final AnnotationSignature annotationSignature) {
        annotationSignatures.add(annotationSignature);
    }

    public void add(final FieldSignature fieldSignature) {
        fieldSignatures.add(fieldSignature);
    }

    public void add(final MethodSignature methodSignature) {
        methodSignatures.add(methodSignature);
    }

    public boolean contains(final MethodSignature methodSignature) {
        return methodSignatures.contains(methodSignature);
    }

    public void addExtends(final String extendsClass) {
        extendedClasses.add(extendsClass);
    }

    public void addImplements(final String implementsClass) {
        implementedClasses.add(implementsClass);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("package ").append(packageName).append(";\n\n");
        if (description != null) {
            sb.append("/**\n * ").append(description).append("\n */\n");
        }

        for (final AnnotationSignature annotationSignature : annotationSignatures) {
            sb.append(annotationSignature.toString()).append('\n');
        }

        sb.append("public ").append(type).append(" ").append(name);
        if (!extendedClasses.isEmpty()) {
            sb.append(" extends ");
            sb.append(String.join(", ", extendedClasses));
        }
        if (!implementedClasses.isEmpty()) {
            sb.append(" implements ");
            sb.append(String.join(", ", implementedClasses));
        }
        sb.append(" {");

        for (final FieldSignature fieldSignature : fieldSignatures) {
            sb.append('\n');
            if (fieldSignature.toJavadoc() != null) {
                sb.append(fieldSignature.toJavadoc()).append('\n');
            }
            sb.append(fieldSignature).append('\n');
        }

        for (final MethodSignature methodSignature : methodSignatures) {
            sb.append('\n');
            if (methodSignature.toJavadoc() != null) {
                sb.append(methodSignature.toJavadoc()).append('\n');
            }
            sb.append(methodSignature).append('\n');
        }

        sb.append("\n\n}");
        return sb.toString();
    }
}
