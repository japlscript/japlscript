/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

/**
 * Super-interface for JaplScript enumerations generated for AppleScript enumerations.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface JaplEnum {

    /**
     * Name of this enumeration element.
     *
     * @return name
     */
    String getName();

    /**
     * AppleScript code for this element.
     *
     * @return code
     */
    String getCode();

    /**
     * Description of this enumeration element.
     *
     * @return description
     */
    String getDescription();
}
