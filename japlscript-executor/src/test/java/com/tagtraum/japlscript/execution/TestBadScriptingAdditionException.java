/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TestBadScriptingAdditionException.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestBadScriptingAdditionException {


	@Test
    public void testCreateException() {
        final String message = "2006-11-05 11:23:02.765 osascript[343] CFLog (21): dyld returns 2 when trying to load " +
                "/System/Library/ScriptingAdditions/StandardAdditions.osax/Contents/MacOS/StandardAdditions";
        assertTrue(BadScriptingAdditionException.isBadScriptingAdditionMessage(message));
        final BadScriptingAdditionException badScriptingAdditionException = new BadScriptingAdditionException(message);
        final List<ScriptingAddition> scriptingAdditions = badScriptingAdditionException.getOffendingScriptingAdditions();
        assertEquals(1, scriptingAdditions.size());
        final ScriptingAddition scriptingAddition = scriptingAdditions.get(0);
        assertEquals(new java.io.File("/System/Library/ScriptingAdditions/StandardAdditions.osax"),
                scriptingAddition.getFolder());

        assertEquals(new java.io.File("/System/Library/ScriptingAdditions/StandardAdditions.osax/" +
                "Contents/MacOS/StandardAdditions"), scriptingAddition.getExecutable());
        //assertTrue(scriptingAddition.isLocalArchitecture());
    }
}
