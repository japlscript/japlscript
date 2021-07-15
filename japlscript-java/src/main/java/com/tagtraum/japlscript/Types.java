/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.*;

import java.util.*;

/**
 * Types.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
final class Types {

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
    private static final Map<String, Class<?>> APPLESCRIPT_TO_JAVA = new HashMap<>();

    static {
        APPLESCRIPT_TO_JAVA.put("boolean", java.lang.Boolean.TYPE);
        APPLESCRIPT_TO_JAVA.put("date", java.util.Date.class);
        APPLESCRIPT_TO_JAVA.put("integer", java.lang.Integer.TYPE);
        APPLESCRIPT_TO_JAVA.put("small integer", Short.TYPE);
        APPLESCRIPT_TO_JAVA.put("double integer", java.lang.Long.TYPE);
        APPLESCRIPT_TO_JAVA.put("unsigned integer", java.lang.Integer.TYPE);
        APPLESCRIPT_TO_JAVA.put("integer or string", String.class);
        APPLESCRIPT_TO_JAVA.put("international text", String.class);
        APPLESCRIPT_TO_JAVA.put("number", java.lang.Double.TYPE);
        APPLESCRIPT_TO_JAVA.put("small real", java.lang.Float.TYPE);
        APPLESCRIPT_TO_JAVA.put("real", java.lang.Double.TYPE);
        APPLESCRIPT_TO_JAVA.put("string", String.class);
        APPLESCRIPT_TO_JAVA.put("text", String.class);
        APPLESCRIPT_TO_JAVA.put("styled text", String.class);
        APPLESCRIPT_TO_JAVA.put("unicode text", String.class);
        APPLESCRIPT_TO_JAVA.put("file specification", java.io.File.class);
        APPLESCRIPT_TO_JAVA.put("list", java.util.List.class);
        APPLESCRIPT_TO_JAVA.put("version", String.class);
        APPLESCRIPT_TO_JAVA.put("point", java.awt.Point.class);
        APPLESCRIPT_TO_JAVA.put("bounding rectangle", java.awt.Rectangle.class);
        APPLESCRIPT_TO_JAVA.put("rectangle", java.awt.Rectangle.class);
        APPLESCRIPT_TO_JAVA.put("type class", TypeClass.class);
        APPLESCRIPT_TO_JAVA.put("type", TypeClass.class);
        APPLESCRIPT_TO_JAVA.put("picture", Picture.class);
        APPLESCRIPT_TO_JAVA.put("rgb color", RGBColor.class);
        APPLESCRIPT_TO_JAVA.put("reference", Reference.class);
        APPLESCRIPT_TO_JAVA.put("specifier", Reference.class);
        // TODO: Anything is actually also literals...
        APPLESCRIPT_TO_JAVA.put("anything", Reference.class);
        APPLESCRIPT_TO_JAVA.put("location reference", LocationReference.class);
        APPLESCRIPT_TO_JAVA.put("alias", Alias.class);
        APPLESCRIPT_TO_JAVA.put("file", JaplScriptFile.class);
        APPLESCRIPT_TO_JAVA.put("tdta", Tdta.class);
        APPLESCRIPT_TO_JAVA.put("raw data", Tdta.class);
        // TODO: check these mappings and complete them.
    }

    private Types() {
    }

    /**
     *
     * @param applescriptType AppleScript type
     * @return the standard Java type or null, if none is defined
     */
    public static String getStandardJavaType(final String applescriptType) {
        if (applescriptType.equalsIgnoreCase("record")) {
            return Map.class.getName() + "<" + String.class.getName() + ", " + Reference.class.getName() + ">";
        }
        final String lowercaseApplescriptType = applescriptType.toLowerCase();
        final Class<?> javaType = APPLESCRIPT_TO_JAVA.get(lowercaseApplescriptType);
        if (javaType != null) return javaType.getName();
        return null;
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
        return Types.toJavaIdentifier(name.toUpperCase());
    }

    /**
     * Converts the name to a camel-cased named with an uppercase first letter.
     *
     * @param name name
     * @return camel-cased name
     */
    public static String toCamelCaseClassName(final String name) {
        return Types.toJavaIdentifier(toCamelCase(name, true));
    }

    /**
     * Converts thh name to a camel-cased named with an lowercase first letter.
     *
     * @param name name
     * @return camel-cased name
     */
    public static String toCamelCaseMethodName(final String name) {
        return Types.toJavaIdentifier(toCamelCase(name, false));
    }

    /**
     * Converts a String to camelcase, by ommitting all non-letter or digit
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
