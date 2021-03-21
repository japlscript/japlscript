/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * TestOsascript.
 * <p/>
 * Date: Jan 7, 2006
 * Time: 4:46:09 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestScriptExecutor {

	@Test
    public void testSimpleScript() throws IOException {
        final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
        scriptExecutor.setScript("return version");
        final String version = scriptExecutor.execute();
        assertNotNull(version);
    }

	@Test
	@Ignore("requires native code")
    public void testSimpleScriptWithError() throws IOException {
        try {
            final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
            scriptExecutor.setScript("return murx version");
            scriptExecutor.execute();
            fail("Expected JaplScriptException");
        } catch (JaplScriptException e) {
            // expected
        }
    }

}
