/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.execution.CocoaScriptExecutor;
import com.tagtraum.japlscript.execution.Osascript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ScriptExecutor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class ScriptExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptExecutor.class);
    private final static List<ExecutionListener> listeners = new ArrayList<ExecutionListener>();
    private static boolean cocoaScriptExecutor;
    static {
        try {
            new CocoaScriptExecutor();
            cocoaScriptExecutor = true;
            if (LOG.isInfoEnabled()) LOG.info("Cocoa AppleScript support active.");
        } catch(Throwable t) {
            cocoaScriptExecutor = false;
            if (LOG.isInfoEnabled()) LOG.info("Cocoa AppleScript support not available. Will use Osascript.");
        }
    }

    private CharSequence script;
    public static final int MAX_RETURNVALUE_LOG_LENGTH = 1024;

    public static void addExecutionListener(final ExecutionListener listener) {
        listeners.add(listener);
    }

    public static boolean removeExecutionListener(final ExecutionListener listener) {
        return listeners.remove(listener);
    }

    public void setScript(final CharSequence script) {
        this.script = script;
        if (LOG.isDebugEnabled()) LOG.debug("Script: " + script);
    }

    public CharSequence getScript() {
        return script;
    }

    /**
     * Execute script.
     *
     * @return return value
     * @throws IOException in case of IO problems
     */
    public String execute() throws IOException {
        final String script = getScript().toString();
        final ExecutionEvent startEvent = new ExecutionEvent(this, script, true, null);
        SwingUtilities.invokeLater(
            () -> {
                for (final ExecutionListener l: listeners) {
                    l.executing(startEvent);
                }
            }
        );
        String result = null;
        try {
            result = executeImpl();
            return result;
        } finally {
            final ExecutionEvent stopEvent = new ExecutionEvent(this, script, false, result);
            SwingUtilities.invokeLater(
                () -> {
                    for (final ExecutionListener l: listeners) {
                        l.executing(stopEvent);
                    }
                }
            );
        }
    }

    /**
     * Actual implementation for the execution.
     *
     * @return return value
     * @throws IOException in case of IO problems
     */
    protected abstract String executeImpl() throws IOException;

    /**
     * Create a new ScriptExecutor.
     *
     * @return script executor
     */
    public static ScriptExecutor newInstance() {
        if (cocoaScriptExecutor) return new CocoaScriptExecutor();
        else return new Osascript();
    }

}
