/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for creating valid Java identifiers.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public final class Identifiers {

    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
        "_",
        "abstract", "assert", "boolean", "break", "byte", "case",
        "catch", "char", "class", "const", "continue", "default",
        "double", "do", "else", "enum", "extends", "false",
        "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long",
        "native", "new", "non-sealed", "null", "package", "private", "protected",
        "public", "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws", "transient",
        "true", "try", "void", "volatile", "while"
    ));

    private Identifiers() {
    }

    /*
    public static String getJavaType(final String applescriptType) {
        final String standardType = getStandardJavaType(applescriptType);
        if (standardType != null) return standardType;
        return toCamelCaseClassName(applescriptType);
    }
    */

    /**
     * Converts a name to a constant name.
     *
     * @param name name
     * @return constant name
     */
    public static String toJavaConstant(final String name) {
        return Identifiers.toJavaIdentifier(name.toUpperCase());
    }

    /**
     * Converts the name to a camel-cased named with an uppercase first letter.
     *
     * @param name name
     * @return camel-cased name
     */
    public static String toCamelCaseClassName(final String name) {
        return Identifiers.toJavaIdentifier(toCamelCase(name, true));
    }

    /**
     * Converts the name to a camel-cased named with a lowercase first letter.
     *
     * @param name name
     * @return camel-cased name
     */
    public static String toCamelCaseMethodName(final String name) {
        return Identifiers.toJavaIdentifier(toCamelCase(name, false));
    }

    /**
     * Converts a String to camelcase, by omitting all non-letter or digit
     * characters and uppercasing the following character.
     *
     * @param identifier identifier
     * @param uppercaseFirstLetter first letter uppercase?
     * @return camel-cased string
     */
    public static String toCamelCase(final String identifier, final boolean uppercaseFirstLetter) {
        final StringBuilder sb = new StringBuilder();
        boolean upperCase = uppercaseFirstLetter;
        for (int i = 0; i < identifier.length(); i++) {
            final char c = identifier.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                upperCase = true;
                continue;
            }
            if (upperCase) sb.append(Character.toUpperCase(c));
            else if (sb.length() == 0) sb.append(Character.toLowerCase(c));
            else sb.append(c);
            upperCase = false;
        }
        return sb.toString();
    }

    /**
     * Converts a string to a valid Java identifier by replacing
     * all disallowed characters to the underscore character.
     *
     * @param string String to convert
     * @return valid Java identifier
     */
    public static String toJavaIdentifier(final String string) {
        final StringBuilder sb = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (Character.isJavaIdentifierPart(c)) sb.append(c);
            else sb.append('_');
        }
        final String s = sb.toString();
        if (JAVA_KEYWORDS.contains(s)) {
            // append _, if the generated identifier is reserved
            return s + "_";
        } else {
            return s;
        }
    }

}
