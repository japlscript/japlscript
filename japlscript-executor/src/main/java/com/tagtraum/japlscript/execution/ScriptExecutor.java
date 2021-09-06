/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract superclass for script executors. Concrete implementation may
 * use different (native) mechanisms to actually run a given script, like
 * the command line tool <code>osascript</code> or
 * <a href="https://developer.apple.com/documentation/foundation/nsapplescript">NSAppleScript</a>.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class ScriptExecutor {

    private static final Logger LOG = Logger.getLogger(ScriptExecutor.class.getName());
    private final static List<ExecutionListener> listeners = new ArrayList<>();
    private static boolean preferOsascript;
    private static boolean cocoaScriptExecutorAvailable;
    public static final int MAX_RETURNVALUE_LOG_LENGTH = 1024;
    static {
        try {
            new CocoaScriptExecutor();
            cocoaScriptExecutorAvailable = true;
            if (LOG.isLoggable(Level.INFO)) LOG.info("Cocoa AppleScript support active.");
        } catch(Throwable t) {
            cocoaScriptExecutorAvailable = false;
            if (LOG.isLoggable(Level.INFO)) LOG.info("Cocoa AppleScript support not available. Will use Osascript.");
        }
    }

    private CharSequence script;

    /**
     * Adds an {@link ExecutionListener} to this executor.
     * Notifications happen on the EDT.
     *
     * @param listener listener to add
     */
    public static void addExecutionListener(final ExecutionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an {@link ExecutionListener} from this executor.
     *
     * @param listener listener to remove
     * @return true, if successfully removed
     */
    public static boolean removeExecutionListener(final ExecutionListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Sets the script to execute.
     *
     * @param script script
     */
    public void setScript(final CharSequence script) {
        this.script = script;
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Script: " + script);
    }

    /**
     * Current script.
     *
     * @return script
     */
    public CharSequence getScript() {
        return script;
    }

    /**
     * Execute the current script.
     *
     * @return return value
     * @throws IOException in case of IO problems
     * @see #getScript()
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
        if (cocoaScriptExecutorAvailable && !preferOsascript) return new CocoaScriptExecutor();
        else return new Osascript();
    }

    /**
     * {@code true}, if we prefer the command line <code>osascript</code>-based executor
     * over the native (Cocoa) executor.
     *
     * @return true or false
     */
    public static boolean isPreferOsascript() {
        return preferOsascript;
    }

    /**
     * Allows using the command line <code>osascript</code>-based executor, even if the
     * native one is available.
     *
     * @param preferOsascript true or false
     * @see #isPreferOsascript()
     */
    public static void setPreferOsascript(final boolean preferOsascript) {
        ScriptExecutor.preferOsascript = preferOsascript;
        if (preferOsascript) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("Prefering Osascript over CocoaScriptExecutor.");
        }
    }

    /**
     * Signals the availability of the native AppleScript executor.
     *
     * @return true or false
     * @see CocoaScriptExecutor
     * @see #isPreferOsascript()
     */
    public static boolean isCocoaScriptExecutorAvailable() {
        return cocoaScriptExecutorAvailable;
    }
}
