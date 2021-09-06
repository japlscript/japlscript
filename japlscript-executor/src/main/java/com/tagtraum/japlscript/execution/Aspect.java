/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

/**
 * Aspect for AppleScript calls.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface Aspect {

    /**
     * AppleScript code to be executed <em>before</em> the body.
     *
     * @param application application, e.g. {@code application "iTunes"}
     * @param body body
     * @return AppleScript code or {@code null}
     */
    String before(final String application, String body);

    /**
     * AppleScript code to be executed <em>after</em> the body.
     *
     * @param application application, e.g. {@code application "iTunes"}
     * @param body body
     * @return AppleScript code or {@code null}
     */
    String after(final String application, String body);

}
