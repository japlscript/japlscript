/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MethodSignature.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class MethodSignature {

    private final String name;
    private final List<ParameterSignature> parameterTypes = new ArrayList<>();
    private final List<AnnotationSignature> annotations = new ArrayList<>();
    private String returnType;
    private String returnTypeDescription;
    private String body;
    private boolean defaultMethod;
    private String description;
    private String visibility;

    public MethodSignature(final String name) {
        Objects.requireNonNull(name, "name is mandatory");
        this.name = name;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(final String visibility) {
        this.visibility = visibility;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getReturnTypeDescription() {
        return returnTypeDescription;
    }

    public void setReturnTypeDescription(final String returnTypeDescription) {
        this.returnTypeDescription = returnTypeDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public boolean isDefaultMethod() {
        return defaultMethod;
    }

    public void setDefaultMethod(final boolean defaultMethod) {
        this.defaultMethod = defaultMethod;
    }

    public void add(final AnnotationSignature annotationSignature) {
        annotations.add(annotationSignature);
    }

    public void setReturnType(final String returnType) {
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public void add(final ParameterSignature type) {
        this.parameterTypes.add(type);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof MethodSignature)) return false;
        final MethodSignature that = (MethodSignature) obj;
        return this.name.equals(that.name)
            && this.returnType.equals(that.returnType)
            && this.parameterTypes.equals(that.parameterTypes);
    }

    public String toJavadoc() {
        final StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        if (description != null) {
            sb.append(" * ").append(description).append('\n');
        }
        sb.append(" *\n");
        for (final ParameterSignature parameterSignature : parameterTypes) {
            sb.append(parameterSignature.toJavadoc()).append('\n');
        }
        if (returnTypeDescription != null) {
            sb.append(" * @return ").append(returnTypeDescription).append('\n');
        }
        sb.append(" */");
        return sb.toString();
    }

    @Override
    public String toString() {
        final List<String> annotations = this.annotations.stream()
            .map(AnnotationSignature::toString)
            .sorted()
            .collect(Collectors.toList());

        final StringBuilder sb = new StringBuilder();
        for (final String annotationSignature : annotations) {
            sb.append(annotationSignature).append('\n');
        }
        if (visibility != null) {
            sb.append(visibility).append(' ');
        }
        if (defaultMethod) {
            sb.append("default ");
        }
        if (returnType != null) {
            sb.append(returnType).append(' ');
        }
        sb.append(name).append('(');
        sb.append(parameterTypes.stream()
            .map(ParameterSignature::toString)
            .collect(Collectors.joining(", ")));
        sb.append(')');
        if (body != null) {
            sb.append(" {\n");
            sb.append("    ").append(body);
            sb.append("\n}");
        } else {
            sb.append(';');
        }
        return sb.toString();
    }
}
