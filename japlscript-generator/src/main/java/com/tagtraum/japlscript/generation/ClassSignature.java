/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import com.tagtraum.japlscript.execution.NativeLibraryLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.tagtraum.japlscript.generation.JavadocSupport.toHTML;

/**
 * Class signature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ClassSignature {

    private final String packageName;
    private final String description;
    private final String name;
    private final String type;
    private final String author;
    private final List<String> extendedClasses = new ArrayList<>();
    private final List<String> implementedClasses = new ArrayList<>();
    private final List<AnnotationSignature> annotationSignatures = new ArrayList<>();
    private final List<MethodSignature> methodSignatures = new ArrayList<>();
    private final List<FieldSignature> fieldSignatures = new ArrayList<>();
    private final List<EnumSignature> enumSignatures = new ArrayList<>();

    public ClassSignature(final String type, final String name, final String packageName, final String description) {
        this(type, name, packageName, description, "JaplScript " + NativeLibraryLoader.VERSION);
    }

    public ClassSignature(final String type, final String name, final String packageName, final String description, final String author) {
        this.packageName = packageName;
        this.description = description;
        this.name = name;
        this.type = type;
        this.author = author;
    }

    public boolean isApplicationClass() {
        return "application".equalsIgnoreCase(name);
    }

    public boolean isScriptingAdditionClass() {
        return "scriptingaddition".equalsIgnoreCase(name);
    }

    public String getName() {
        return this.name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getAuthor() {
        return author;
    }

    public List<MethodSignature> getMethodSignatures() {
        return methodSignatures;
    }

    public String getFullyQualifiedClassName() {
        return packageName + "." + name;
    }

    public void add(final AnnotationSignature annotationSignature) {
        annotationSignatures.add(annotationSignature);
    }

    public void add(final EnumSignature enumSignature) {
        enumSignatures.add(enumSignature);
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

        if (description != null || author != null) {
            sb.append("/**\n");
        }
        if (description != null) {
            sb.append(" * ").append(toHTML(description)).append("\n");
        }
        if (author != null) {
            sb.append(" *\n * @author ").append(author).append("\n");;
        }
        if (description != null || author != null) {
            sb.append(" */\n");
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
        sb.append(" {\n");

        if (!enumSignatures.isEmpty()) {
            sb.append(enumSignatures
                .stream()
                .map(EnumSignature::toString)
                .collect(Collectors.joining(",\n    ", "\n    ", ";\n"))
            );
        }

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

        sb.append("\n}");
        return sb.toString();
    }
}
