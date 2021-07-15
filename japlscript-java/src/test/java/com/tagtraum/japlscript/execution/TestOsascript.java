package com.tagtraum.japlscript.execution;

import com.tagtraum.japlscript.JaplScript;
import com.tagtraum.japlscript.JaplScriptException;
import com.tagtraum.japlscript.ScriptExecutor;
import com.tagtraum.japlscript.Session;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * TestOsascript.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestOsascript {

    @Before
    public void setUp() {
        final Session session = Session.getSession();
        if (session != null) session.setCompile(false);
    }

	@Test
    public void testSimpleScript() throws IOException {
        final ScriptExecutor scriptExecutor = new Osascript();
        scriptExecutor.setScript("return version");
        final String version = scriptExecutor.execute();
        assertNotNull(version);
    }

	@Test
    public void testSimpleCompiledScript() throws IOException {
        final Session session = JaplScript.startSession();
        session.setCompile(true);
        final ScriptExecutor scriptExecutor = new Osascript();
        scriptExecutor.setScript("return version");
        final String version = scriptExecutor.execute();
        assertNotNull(version);
    }

	@Test
	public void testSimpleScriptWithError() throws IOException {
        try {
            final ScriptExecutor scriptExecutor = new Osascript();
            scriptExecutor.setScript("return murx version");
            scriptExecutor.execute();
            fail("Expected JaplScriptException");
        } catch (JaplScriptException e) {
            // expected this
        }
    }

    /*
	@Test
    public void testSpeed() throws IOException {
        Session session = JaplScript.startSession();
        session.setCompile(false);
        Osascript osascript = new Osascript("return version");
        long start = System.currentTimeMillis();
        for (int i=0; i<10; i++) osascript.execute();
        System.out.println("uncompiled: " + (System.currentTimeMillis() - start));

        session.setCompile(true);
        start = System.currentTimeMillis();
        for (int i=0; i<10; i++) osascript.execute();
        System.out.println("compiled  : " + (System.currentTimeMillis() - start));
    }
    */

	@Test
	public void testOsascriptSpeed() throws IOException {
        final ScriptExecutor scriptExecutor = new Osascript();
        scriptExecutor.setScript("return version");
        long start = System.currentTimeMillis();
        for (int i=0; i<5; i++) scriptExecutor.execute();
        System.out.println("version * 5 using " + scriptExecutor.getClass().getName() + ": "
                + (System.currentTimeMillis() - start));
    }

	@Test
	public void testScriptExecutorSpeed() throws IOException {
        final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
        scriptExecutor.setScript("return version");
        long start = System.currentTimeMillis();
        for (int i=0; i<5; i++) scriptExecutor.execute();
        System.out.println("version * 5 using " + scriptExecutor.getClass().getName() + ": "
                + (System.currentTimeMillis() - start));
    }

    @Test
    public void testGetRecord() throws IOException {
        final ScriptExecutor scriptExecutor = new Osascript();
        final String script = "tell application \"Finder\"\n" +
            "    return properties of first item of startup disk\n" +
            "end tell";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();

        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("class:"));
    }

}
