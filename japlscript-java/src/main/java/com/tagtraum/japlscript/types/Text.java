/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplScript;
import com.tagtraum.japlscript.JaplType;

/**
 * Text.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Text implements JaplType<String> {

    private static final Text instance = new Text();

    private Text() {
    }

    public static Text getInstance() {
        return instance;
    }


    @Override
    public String _parse(final String objectReference, final String applicationReference) {
        String trimmed = objectReference == null ? null : objectReference.trim();
        if (trimmed != null && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    @Override
    public String _encode(final Object s) {
        return s == null
            ? "null" // or "missing value"?
            : JaplScript.quote((String)s);
    }

    @Override
    public Class<String> _getInterfaceType() {
        return String.class;
    }
}