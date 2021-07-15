/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import com.tagtraum.japlscript.ScriptExecutor;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TestCocoaScriptExecutor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestCocoaScriptExecutor {


    @Test
    public void testReturnHello() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "return \"hello\"";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);
        assertEquals(result, "hello");
    }

    @Test
    public void testReturnQuotedString() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "return \"hel\\\"lo\"";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);
        assertEquals(result, "hel\"lo");
    }

    @Test
    public void testReturnNothing() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "return";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);
        assertTrue(result == null || result.isEmpty());
    }

    @Test
    public void testGetRecord() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "tell application \"Finder\"\n" +
            "    return properties of (path to home folder)\n" +
            "end tell";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);

        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("«property pcls»:"));
    }

    @Test
    public void testGetRecord2() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "return {blub: \"hallo\", goo: {gloo: 6}}";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);

        assertEquals("{blub: \"hallo\", goo: {gloo: 6}}", result);
    }

    @Test
    public void testGetRecordWithMissingValue() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "return {a: missing value}";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);

        assertEquals("{a: missing value}", result);
    }

    @Test
    public void testReturnMissingValue() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "return missing value";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);

        assertEquals("missing value", result);
    }

}
