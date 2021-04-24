/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import com.tagtraum.japlscript.ScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    private static final Logger LOG = LoggerFactory.getLogger(CocoaScriptExecutor.class);

    @Override
    public String executeImpl() throws IOException {
        final String returnValue = execute(getScript().toString());
        if (LOG.isDebugEnabled() && returnValue != null && returnValue.length() > 0) {
            LOG.debug("Return value: " + returnValue.substring(0, Math.min(MAX_RETURNVALUE_LOG_LENGTH,
                    returnValue.length())));
        }
        return returnValue;
    }

    private native String execute(final String script) throws IOException;

}
