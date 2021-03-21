/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Type.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {
    /**
     * Value.
     *
     * @return value
     */
    String value();
}
