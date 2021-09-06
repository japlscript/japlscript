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

/**
 * Per-thread session.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Session {

    private static final ThreadLocal<Session> SESSIONS = new ThreadLocal<Session>();
    public static final int DEFAULT_TIMEOUT = -1;
    private StringBuilder script;
    private boolean ignoreReturnValues;
    private boolean compile;
    private int timeout = DEFAULT_TIMEOUT;
    private final List<Aspect> aspects = new ArrayList<Aspect>();

    /**
     * Session.
     */
    public Session() {
        SESSIONS.set(this);
        reset();
    }

    public static Session startSession() {
        Session session = Session.getSession();
        if (session == null) {
            session = new Session();
        }
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
        return new ArrayList<Aspect>(aspects);
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
        if (getSession() == null) throw new IllegalStateException("Session already committed.");
        this.script.append(applescriptFragment).append("\n");
    }

    /**
     * Resets the session. All previously added, but uncommitted fragments are lost.
     */
    public void reset() {
        this.script = new StringBuilder();
        this.timeout = DEFAULT_TIMEOUT;
    }

    /**
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
        try {
            if (script.length() > 0) {
                final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
                scriptExecutor.setScript(script);
                scriptExecutor.execute();
            }
        } catch (IOException e) {
            throw new JaplScriptException(e);
        } finally {
            script = new StringBuilder();
            SESSIONS.remove();
        }
    }

    /**
     *
     * @return this thread's session.
     */
    public static Session getSession() {
        return SESSIONS.get();
    }

}
