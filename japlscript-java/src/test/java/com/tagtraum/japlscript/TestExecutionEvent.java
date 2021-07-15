/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TestExecutionEvent.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestExecutionEvent {

    @Test
    public void testBasics() {
        final ExecutionEvent event0 = new ExecutionEvent("source", "script", true, "false");
        final ExecutionEvent event1 = new ExecutionEvent("source", "script", true, "false");
        final ExecutionEvent event2 = new ExecutionEvent("source", "script", false, "true");
        final ExecutionEvent event3 = new ExecutionEvent("source", "script", false, null);
        assertEquals(event0.hashCode(), event1.hashCode());
        assertTrue(event0.equals(event0));
        assertTrue(event0.equals(event1));
        assertFalse(event0.equals(event2));
        assertFalse(event0.equals(event3));
        assertFalse(event0.equals("some String"));
        assertFalse(event0.equals(null));
        assertEquals(event0, event1);
        assertNotEquals(event0, event2);
        assertNotNull(event0.toString());
        assertTrue(event0.isStarted());
        assertFalse(event0.isFinished());
        assertFalse(event2.isStarted());
        assertTrue(event2.isFinished());
    }

    @Test
    public void testHashCode() {
        new ExecutionEvent("source", "script", true, null).hashCode();
        new ExecutionEvent("source", "script", true, "not null").hashCode();
        new ExecutionEvent("source", "script", false, null).hashCode();
        new ExecutionEvent("source", "script", false, "not null").hashCode();
    }
}
