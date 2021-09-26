/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

/**
 * Help creating valid Javadocs.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class JavadocSupport {

    private JavadocSupport() {
    }

    /**
     * Escape String to HTML.
     *
     * @param string string to escape
     * @return escaped String
     */
    public static String toHTML(final String string) {
        if (string == null) return null;
        final StringBuilder sb = new StringBuilder(string.length());
        final int len = string.length();
        char c;
        for (int i = 0; i < len; i++) {
            c = string.charAt(i);
            // HTML Special Chars
            if (c == '"') {
                sb.append("&quot;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\n') {
                // Handle Newline
                sb.append("<br/>");
            } else {
                int ci = 0xffff & c;
                if (ci < 160) {
                    // nothing special only 7 Bit
                    sb.append(c);
                } else {
                    // Not 7 Bit use the unicode system
                    sb.append("&#");
                    sb.append(ci);
                    sb.append(';');
                }
            }
        }
        return sb.toString();
    }
}
