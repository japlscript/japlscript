/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.Chevron;
import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.japlscript.Codec;

/**
 * Point.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Point implements Codec<java.awt.Point> {

    private static final Point instance = new Point();
    private static final TypeClass[] CLASSES = {
        new TypeClass("point", new Chevron("class", "QDpt"))
    };

    private Point() {
    }

    public static Point getInstance() {
        return instance;
    }

    @Override
    public java.awt.Point _decode(final String objectReference, final String applicationReference) {
        final String t = objectReference.trim();
        if (t.isEmpty()) return null;
        if (t.startsWith("{") && t.endsWith("}")) {
            final String sub = t.substring(1, t.length() - 1);
            final int comma = sub.indexOf(',');
            final int x = java.lang.Integer.parseInt(sub.substring(0, comma).trim());
            final int y = java.lang.Integer.parseInt(sub.substring(comma + 1).trim());
            return new java.awt.Point(x, y);
        } else {
            throw new JaplScriptException("Failed to parse point: " + objectReference);
        }
    }

    @Override
    public String _encode(final Object object) {
        if (object == null) return "null";
        final java.awt.Point point = (java.awt.Point)object;
        return "{" + point.x + ", " + point.y + "}";
    }

    @Override
    public Class<java.awt.Point> _getJavaType() {
        return java.awt.Point.class;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }
}
