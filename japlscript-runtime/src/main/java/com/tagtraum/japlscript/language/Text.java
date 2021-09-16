/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.Chevron;
import com.tagtraum.japlscript.JaplScript;
import com.tagtraum.japlscript.Codec;

/**
 * Text (=String).
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Text implements Codec<String> {

    private static final Text instance = new Text();
    private static final TypeClass[] CLASSES = {
        new TypeClass("text", new Chevron("class", "ctxt").toString(), null, null),
        new TypeClass("unicode text", new Chevron("class", "utxt").toString(), null, null),
        new TypeClass("styled unicode text", new Chevron("class", "sutx").toString(), null, null),
        new TypeClass("styled text", new Chevron("class", "STXT").toString(), null, null),
        new TypeClass("international text", new Chevron("class", "itxt").toString(), null, null),
        new TypeClass("number or string", new Chevron("class", "ns  ").toString(), null, null),
        new TypeClass("list or string", new Chevron("class", "ls  ").toString(), null, null),
        new TypeClass("alias or string", new Chevron("class", "sf  ").toString(), null, null),
        new TypeClass("string", new Chevron("class", "TEXT").toString(), null, null),
        new TypeClass("version", new Chevron("class", "vers").toString(), null, null),
        new TypeClass("version", new Chevron("class", "vers").toString(), null, null),
        // TODO: add more from https://gist.github.com/ccstone/955a0461d0ba02289b0cef469862ec84 ?
    };

    private Text() {
    }

    public static Text getInstance() {
        return instance;
    }


    @Override
    public String _decode(final String objectReference, final String applicationReference) {
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
    public Class<String> _getJavaType() {
        return String.class;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }
}