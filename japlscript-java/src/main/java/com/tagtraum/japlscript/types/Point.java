/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplScriptException;
import com.tagtraum.japlscript.JaplType;

/**
 * Point.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Point implements JaplType<java.awt.Point> {

    private static final Point instance = new Point();

    private Point() {
    }

    public static Point getInstance() {
        return instance;
    }

    @Override
    public java.awt.Point _parse(final String objectReference, final String applicationReference) {
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
    public Class<java.awt.Point> _getInterfaceType() {
        return java.awt.Point.class;
    }
}
