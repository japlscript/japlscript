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
 * Kind annotation (for example, "command", "element", "property") for generated
 * methods.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Kind {

    /**
     * Value.
     *
     * @return value
     */
    String value();
}
