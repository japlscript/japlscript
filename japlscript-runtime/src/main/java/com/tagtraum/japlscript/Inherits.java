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
 * Inherits annotation for generated interfaces.
 * Describes the AppleScript type that the AppleScript type
 * of annotated interface inherits from.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inherits {

    /**
     * Value.
     *
     * @return value
     */
    String value();
}
