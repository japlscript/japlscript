/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The plural form of an AppleScript name.
 * For example: {@code Artwork} (singular) -&gt; {@code Artworks} (plural).
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plural {

    /**
     * Value.
     *
     * @return value
     */
    String value();
}
