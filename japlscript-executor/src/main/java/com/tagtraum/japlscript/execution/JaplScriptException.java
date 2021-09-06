/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JaplScript exception.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class JaplScriptException extends RuntimeException {

    private final String error;
    private final String script;

    public JaplScriptException(final Throwable throwable) {
        super(throwable);
        this.error = getMessage();
        this.script = null;
    }

    /**
     *
     * @param errorMessage AppleScript error message
     * @param script script causing the error
     */
    public JaplScriptException(final String errorMessage, final String script) {
        super(convertUnicode(errorMessage) + "\nScript:\n" + script);
        this.script = script;
        this.error = convertUnicode(errorMessage);
    }

    public JaplScriptException(final String message) {
        super(convertUnicode(message));
        this.error = convertUnicode(message);
        this.script = null;
    }

    public JaplScriptException(final String errorMessage, final Throwable cause) {
        super(convertUnicode(errorMessage), cause);
        this.error = convertUnicode(errorMessage);
        this.script = null;
    }

    /**
     *
     * @return error message
     */
    public String getError() {
        return error;
    }

    /**
     *
     * @return causing script
     */
    public String getScript() {
        return script;
    }

    /**
     * Convert unicode sequences to their actual characters.
     *
     * @param s string containing sequences like {@code \U2016}
     * @return plain char string
     */
    private static String convertUnicode(final String s) {
        final Pattern pattern = Pattern.compile("\\\\U[0-9[a-f][A-F]]{4}");
        final Matcher matcher = pattern.matcher(s);
        final Set<String> unicodeSequence = new HashSet<>();
        while (matcher.find()) {
            unicodeSequence.add(matcher.group());
        }
        String result = s;
        for (final String unicode : unicodeSequence) {
            final char c = (char)Integer.parseInt(unicode.substring(2), 16);
            result = result.replaceAll("\\" + unicode, Character.toString(c));
        }
        return result;
    }

}
