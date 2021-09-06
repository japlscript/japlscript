/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cocoa based AppleScript executor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class CocoaScriptExecutor extends ScriptExecutor {

    static {
        // Ensure JNI library is loaded
        NativeLibraryLoader.loadLibrary();
    }
    private static final Logger LOG = Logger.getLogger(CocoaScriptExecutor.class.getName());

    @Override
    public String executeImpl() throws IOException {
        String returnValue = execute(getScript().toString());
        if (returnValue != null) {
            returnValue = returnValue.trim();
        }
        if (LOG.isLoggable(Level.FINE) && returnValue != null && !returnValue.isEmpty()) {
            LOG.fine("Return value: " + returnValue.substring(0, Math.min(MAX_RETURNVALUE_LOG_LENGTH,
                    returnValue.length())));
        }
        return returnValue;
    }

    private native String execute(final String script) throws IOException;

}
