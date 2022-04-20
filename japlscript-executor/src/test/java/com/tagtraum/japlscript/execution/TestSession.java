/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestSession.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestSession {

    @Test
    public void testAddRemoveAspect() {
        final Session session = Session.startSession();
        final Aspect testAspect = new Aspect() {
            @Override
            public String before(final String application, final String body) {
                return null;
            }

            @Override
            public String after(final String application, final String body) {
                return null;
            }
        };
        session.addAspect(testAspect);
        assertTrue(session.getAspects().contains(testAspect));
        assertTrue(session.removeAspect(testAspect));
        assertFalse(session.removeAspect(testAspect));
    }

    @Test
    public void testSetTimeout() {
        final Session session = Session.startSession();
        assertTrue(session.isDefaultTimeout());
        assertEquals(-1, session.getTimeout());
        session.setTimeout(5);
        assertFalse(session.isDefaultTimeout());
        assertEquals(5, session.getTimeout());
        final Aspect timeout = session.getAspects().stream().filter(s -> s instanceof Timeout).findFirst().orElse(null);
        assertNotNull(timeout);
        assertEquals(5, ((Timeout)timeout).getSeconds());
        session.setTimeout(-5);
    }

    @Test
    public void testSetIgnoreReturnValues() {
        final Session session = Session.startSession();
        assertFalse(session.isIgnoreReturnValues());
        session.setIgnoreReturnValues(true);
        assertTrue(session.isIgnoreReturnValues());
    }

    @Test
    public void testCommit() throws InvocationTargetException, InterruptedException {
        // ensure the session is empty
        final Session s = Session.getSession();
        if (s != null) {
            s.commit();
        }

        final TestExecutionListener listener = new TestExecutionListener();
        ScriptExecutor.addExecutionListener(listener);
        final Session session = Session.startSession();

        session.add("return \"123\"");
        session.add("return \"456\"");

        // wait until all events are delivered on EDT
        SwingUtilities.invokeAndWait(() -> { });

        assertTrue(listener.getEvents().isEmpty(), "Found the following events: " + listener.getEvents());

        session.commit();

        // wait until all events are delivered on EDT
        SwingUtilities.invokeAndWait(() -> { });

        assertEquals(2, listener.getEvents().size());
    }

    @Test
    public void testSessionAlreadyCommitted() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            final Session session = Session.startSession();
            session.add("return \"123\"");
            session.commit();

            session.add("return \"456\"");
        });
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

    @Test
    public void testSetCompile() {
        final Session session = Session.startSession();
        try {
            session.setCompile(false);
            assertFalse(session.isCompile());
            session.setCompile(true);
            assertTrue(session.isCompile());

            session.add("return version");
        } finally {
            session.commit();
        }
    }

    @Test
    public void testIsStarted() {
        Session session = Session.getSession();
        try {
            if (session != null) session.reset();
            assertFalse(Session.isStarted());
            session = Session.startSession();
            assertFalse(Session.isStarted());
            session.add("set a to be");
            assertTrue(Session.isStarted());
        } finally {
            try {
                if (session != null) session.commit();
            } catch (Exception e) {
                // a failure here does not really interest us
                // in this test case
                e.printStackTrace();
            }
        }
    }
}
