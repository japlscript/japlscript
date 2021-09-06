/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestScriptExecutor.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestScriptExecutor {

    @Test
    public void testPreferOsascript() {
        ScriptExecutor.setPreferOsascript(true);
        assertTrue(ScriptExecutor.isPreferOsascript());
        assertEquals(Osascript.class, ScriptExecutor.newInstance().getClass());
        ScriptExecutor.setPreferOsascript(false);
        assertFalse(ScriptExecutor.isPreferOsascript());
        if (ScriptExecutor.isCocoaScriptExecutorAvailable()) {
            assertEquals(CocoaScriptExecutor.class, ScriptExecutor.newInstance().getClass());
        } else {
            System.out.println("Unable to test CocoaScriptExecutor, as it is unavailable");
        }
    }

    @Test
    public void testCocoaScriptExecutorAvailable() {
        boolean available = false;
        try {
            new CocoaScriptExecutor();
            available = true;
        } catch(Throwable t) {
            // ignore
        }
        assertEquals(available, ScriptExecutor.isCocoaScriptExecutorAvailable());
    }

    @Test
    public void testSimpleScript() throws IOException {
        final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
        scriptExecutor.setScript("return version");
        final String version = scriptExecutor.execute();
        assertNotNull(version);
    }

	@Test
	@Disabled("requires native code")
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

    @Test
    public void testRemoveExecutionListener() {
        final TestExecutionListener listener = new TestExecutionListener();
        ScriptExecutor.addExecutionListener(listener);
        assertTrue(ScriptExecutor.removeExecutionListener(listener));
        assertFalse(ScriptExecutor.removeExecutionListener(listener));
    }

    @Test
    public void testEvents() throws IOException, InvocationTargetException, InterruptedException {
        final TestExecutionListener listener = new TestExecutionListener();
        ScriptExecutor.addExecutionListener(listener);
        final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
        final String script = "return version";
        scriptExecutor.setScript(script);
        final String version = scriptExecutor.execute();
        assertNotNull(version);

        // wait until all events are delivered on EDT
        SwingUtilities.invokeAndWait(() -> { });

        assertEquals(2, listener.getEvents().size());
        final ExecutionEvent firstEvent = listener.getEvents().get(0);
        assertEquals(new ExecutionEvent(scriptExecutor, script, true, null),
            firstEvent);
        assertEquals(new ExecutionEvent(scriptExecutor, script, false, version),
            listener.getEvents().get(1));

        assertEquals(scriptExecutor, firstEvent.getSource());
        assertEquals(script, firstEvent.getScript());
        assertNull(firstEvent.getResult());
        assertTrue(firstEvent.isStarted());
        assertFalse(firstEvent.isFinished());
    }

    private static class TestExecutionListener implements ExecutionListener {
	    private final List<ExecutionEvent> events = new ArrayList<>();

        @Override
        public void executing(final ExecutionEvent e) {
            events.add(e);
        }

        public List<ExecutionEvent> getEvents() {
            return events;
        }
    }

}
