/*
 * =================================================
 * Copyright 2020 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

/**
 * Listens to AppleScript executions.
 * Note that events are delivered asynchronously on the EDT,
 * so that this can be used in UI elements.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface ExecutionListener {

    /**
     * Is called before and after the execution of a script.
     *
     * @param e event
     */
    void executing(ExecutionEvent e);

}
