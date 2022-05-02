/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Per-thread session.
 * Allows the executions of multiple setters or commands in a <em>single</em>
 * AppleScript snippet, thus saving some roundtrips.
 * <p>
 * To use,
 * <ul>
 * <li>call <code>Session session = {@link #startSession()};</code> ,</li>
 * <li>call any number of setters or commands that don't have return values</li>
 * <li>then call <code>session.{@link #commit()};</code> to execute all calls in one AppleScript</li>
 * </ul>
 * <p>
 * In order to "rollback" any uncommitted AppleScript fragments, call {@link #reset()}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Session {

    private static final Logger LOG = Logger.getLogger(Session.class.getName());
    private static final ThreadLocal<Session> SESSIONS = new ThreadLocal<>();
    public static final int DEFAULT_TIMEOUT = -1;
    private StringBuilder script;
    private boolean ignoreReturnValues;
    private boolean compile;
    private int timeout = DEFAULT_TIMEOUT;
    private final List<Aspect> aspects = new ArrayList<>();

    /**
     * Session.
     */
    public Session() {
        SESSIONS.set(this);
        reset();
    }

    public static Session startSession() {
        Session session = Session.get();
        if (session == null) {
            session = new Session();
        } else {
            if (session.script.length() != 0) {
                LOG.warning("Session.startSession(): Uncommitted fragments in current session will be lost. " +
                    "Call Session.getSession().commit() or Session.getSession().reset() before starting a new session. " +
                    "Lost fragments: " + session.script);
            }
            session.reset();
        }
        LOG.fine("Starting new session for thread " + Thread.currentThread().getName());
        return session;
    }

    public void addAspect(final Aspect aspect) {
        this.aspects.add(aspect);
    }

    public boolean removeAspect(final Aspect aspect) {
        return aspects.remove(aspect);
    }

    /**
     * @return copy of the aspect list
     */
    public List<Aspect> getAspects() {
        return new ArrayList<>(aspects);
    }

    /**
     * @return timeout in seconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout in seconds
     */
    public void setTimeout(final int timeout) {
        aspects.removeIf(aspect -> aspect instanceof Timeout);
        if (timeout>0) aspects.add(0, new Timeout(timeout));
        this.timeout = timeout;
        LOG.fine("Setting timeout to " + timeout + " thread " + Thread.currentThread().getName());
    }

    public boolean isDefaultTimeout() {
        return timeout <= 0;
    }

    public boolean isIgnoreReturnValues() {
        return ignoreReturnValues;
    }

    public void setIgnoreReturnValues(final boolean ignoreReturnValues) {
        this.ignoreReturnValues = ignoreReturnValues;
    }

    public boolean isCompile() {
        return compile;
    }

    public void setCompile(final boolean compile) {
        this.compile = compile;
    }

    /**
     * Adds an AppleScript fragment to the session.
     *
     * @param applescriptFragment AppleScript fragment
     */
    public void add(final CharSequence applescriptFragment) {
        if (get() == null) {
            throw new IllegalStateException("Session already committed.");
        }
        this.script.append(applescriptFragment).append("\n");
        LOG.fine("Adding fragment to session for thread "
            + Thread.currentThread() + ": \"" + applescriptFragment + "\"");
    }

    /**
     * Resets the session. All previously added, but uncommitted fragments are lost.
     */
    public void reset() {
        LOG.fine("Resetting session for thread " + Thread.currentThread().getName());
        this.script = new StringBuilder();
        this.timeout = DEFAULT_TIMEOUT;
    }

    /**
     * Indicates whether there is already a started session belonging to the current thread.
     * "Started" means that AppleScript fragments have already been added that would be
     * discarded upon a call of {@link Session#startSession()}.
     *
     * @return true, if there is already a started session belonging to this thread
     */
    public static boolean isStarted() {
        final Session session = get();
        return session != null && session.script.length() != 0;
    }

    /**
     * Return the current script or script fragments.
     *
     * @return current script
     */
    public String getScript() {
        return script.toString();
    }

    /**
     * Executes the current script (collection of fragments)
     * and removes the registered session.
     */
    public void commit() {
        LOG.fine("Committing session for thread " + Thread.currentThread().getName());
        try {
            if (script.length() > 0) {
                final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
                scriptExecutor.setScript(script);
                scriptExecutor.execute();
            } else {
                LOG.fine("Committing empty session.");
            }
        } catch (IOException e) {
            throw new JaplScriptException(e);
        } finally {
            script = new StringBuilder();
            SESSIONS.remove();
        }
    }

    /**
     * Deprecated. Use {@link #get()} instead.
     *
     * @return this thread's session, or {@code null}, if there is none
     */
    @Deprecated(forRemoval = true, since = "3.4.12")
    public static Session getSession() {
        return Session.get();
    }

    /**
     * If there is a session associated with the current thread,
     * return that session. Otherwise return {@code null}.
     *
     * @return this thread's session, or {@code null}, if there is none
     */
    public static Session get() {
        return SESSIONS.get();
    }

}
