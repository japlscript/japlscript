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
 * Annotation that specifies the AppleScript type of something, for example a property.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Type {

    /**
     * Value.
     *
     * @return value
     */
    String value();
}
