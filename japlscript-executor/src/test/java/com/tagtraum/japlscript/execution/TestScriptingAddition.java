/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

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
            assertTrue(scriptingAddition.getArchitectures().contains(ScriptingAddition.Architecture.X86_64));
        }
        if (arch.equals("aarch64")) {
            assertTrue(scriptingAddition.getArchitectures().contains(ScriptingAddition.Architecture.AARCH64));
        }
        assertTrue(scriptingAddition.isLocalArchitecture());
    }

    @Test
    public void testBasics() {
        final java.io.File file = new File("/System/Library/ScriptingAdditions/StandardAdditions.osax/" +
            "Contents/MacOS/StandardAdditions");
        final ScriptingAddition scriptingAddition = new ScriptingAddition(file);
        assertTrue(scriptingAddition.toString().contains(" binary]"));
    }

    @Test
    public void testScriptingAdditionFolder() {
        final java.io.File file = new File("/System/Library/ScriptingAdditions/StandardAdditions.osax");
        final ScriptingAddition scriptingAddition = new ScriptingAddition(file);
        assertNull(scriptingAddition.getExecutable());
        assertEquals(file, scriptingAddition.getFolder());
        assertTrue(scriptingAddition.getArchitectures().contains(ScriptingAddition.Architecture.UNKNOWN));
    }

}
