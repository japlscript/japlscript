/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * TestScriptingAddition.
 *
 * Date: Nov 5, 2006
 * Time: 5:15:40 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestScriptingAddition {

	@Test
	public void testStandardAdditions() {
        final java.io.File file = new File("/System/Library/ScriptingAdditions/StandardAdditions.osax/" +
                "Contents/MacOS/StandardAdditions");
        final ScriptingAddition scriptingAddition = new ScriptingAddition(file);
        assertEquals(new java.io.File("/System/Library/ScriptingAdditions/StandardAdditions.osax"),
		        scriptingAddition.getFolder());
        assertEquals(file, scriptingAddition.getExecutable());
        //assertTrue(scriptingAddition.isLocalArchitecture());
    }

}
