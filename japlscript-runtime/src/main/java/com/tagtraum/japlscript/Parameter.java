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
 * Parameter annotation for translating Java parameter position to AppleScript parameters.
 * E.g. position 2 -&gt; "using".
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

    /**
     * Value.
     *
     * @return value
     */
    String value();
}
