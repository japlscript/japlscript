/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import com.tagtraum.japlscript.JaplScriptException;
import com.tagtraum.japlscript.JaplType;

import java.awt.*;

/**
 * RGBColor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RGBColor implements JaplType<Color> {

    private static final RGBColor instance = new RGBColor();

    private RGBColor() {
    }

    public static RGBColor getInstance() {
        return instance;
    }

    @Override
    public java.awt.Color _parse(final String objectReference, final String applicationReference) {
        final String t = objectReference.trim();
        if (t.isEmpty()) return null;
        if (t.startsWith("{") && t.endsWith("}")) {
            final String sub = t.substring(1, t.length() - 1);
            final String[] parts = sub.split(",");
            final int r = java.lang.Integer.parseInt(parts[0].trim());
            final int g = java.lang.Integer.parseInt(parts[1].trim());
            final int b = java.lang.Integer.parseInt(parts[2].trim());
            return new java.awt.Color(r/65535f, g/65535f, b/65535f);
        } else {
            throw new JaplScriptException("Failed to parse RGBColor: " + objectReference);
        }
    }

    @Override
    public String _encode(final Object object) {
        if (object == null) return "null";
        final java.awt.Color color = (java.awt.Color)object;
        final float[] rgbColorComponents = color.getRGBColorComponents(null);
        return "{" + (int) (rgbColorComponents[0] * 65535f - 0.5)
            + ", " + (int) (rgbColorComponents[1] * 65535f - 0.5)
            + ", " + (int) (rgbColorComponents[2] * 65535f - 0.5)
            + "}";
    }

    @Override
    public Class<java.awt.Color> _getInterfaceType() {
        return java.awt.Color.class;
    }}
