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
import static org.junit.Assert.assertTrue;

/**
 * TestScriptingAddition.
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
        final String arch = System.getProperty("os.arch");
        if (arch.equals("x86_64")) {
            assertEquals(ScriptingAddition.Architecture.X86_64, scriptingAddition.getArchitecture());
            assertEquals("[X86_64 binary]: /System/Library/ScriptingAdditions/StandardAdditions.osax", scriptingAddition.toString());
        }
        if (arch.equals("aarch64")) {
            assertEquals(ScriptingAddition.Architecture.AARCH64, scriptingAddition.getArchitecture());
            assertEquals("[AARCH64 binary]: /System/Library/ScriptingAdditions/StandardAdditions.osax", scriptingAddition.toString());
        }
        assertTrue(scriptingAddition.isLocalArchitecture());

    }

}
