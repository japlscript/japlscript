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
 * Rectangle.
 *
 * Note that in AppleScript a rectangle is defined by its top left
 * and bottom right corners and not by the top left corner and
 * width/height.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Rectangle implements Codec<java.awt.Rectangle> {

    private static final Rectangle instance = new Rectangle();
    private static final TypeClass[] CLASSES = {
        new TypeClass("bounding rectangle", new Chevron("class", "qdrt")),
        new TypeClass("rectangle", new Chevron("class", "qdrt"))
    };

    private Rectangle() {
    }

    /**
     * Null instance used for {@link Codec} implementation.
     *
     * @return null instance
     */
    public static Rectangle getInstance() {
        return instance;
    }

    @Override
    public java.awt.Rectangle _decode(final String objectReference, final String applicationReference) {
        final String t = objectReference.trim();
        if (t.isEmpty()) return null;
        if (t.startsWith("{") && t.endsWith("}")) {
            final String sub = t.substring(1, t.length() - 1);
            final String[] parts = sub.split(",");
            final int x0 = java.lang.Integer.parseInt(parts[0].trim());
            final int y0 = java.lang.Integer.parseInt(parts[1].trim());
            final int x1 = java.lang.Integer.parseInt(parts[2].trim());
            final int y1 = java.lang.Integer.parseInt(parts[3].trim());
            return new java.awt.Rectangle(x0, y0, x1-x0, y1-y0);
        } else {
            throw new JaplScriptException("Failed to parse rectangle: " + objectReference);
        }
    }

    @Override
    public String _encode(final Object object) {
        if (object == null) return "null";
        final java.awt.Rectangle rectangle = (java.awt.Rectangle)object;
        return "{" + rectangle.x
            + ", " + rectangle.y
            + ", " + (rectangle.x + rectangle.width)
            + ", " + (rectangle.y + rectangle.height)
            + "}";
    }

    @Override
    public Class<java.awt.Rectangle> _getJavaType() {
        return java.awt.Rectangle.class;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }
}
