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

/**
 * TestCocoaScriptExecutor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestCocoaScriptExecutor {


    @Test
    public void test() throws IOException {
        final ScriptExecutor scriptExecutor = new CocoaScriptExecutor();
        final String script = "return \"hello\"";
        scriptExecutor.setScript(script);
        assertEquals(script, scriptExecutor.getScript());
        final String result = scriptExecutor.execute();
        System.out.println(result);
        assertEquals(result, "hello");
    }
}
