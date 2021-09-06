/*
 * =================================================
 * Copyright 2020 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.util.Objects;

/**
 * Execution event.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ExecutionEvent {
    private final Object source;
    private final String script;
    private final boolean started;
    private final String result;

    public ExecutionEvent(final Object source, final String script, final boolean started, final String result) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(script);
        this.source = source;
        this.script = script;
        this.started = started;
        this.result = result;
    }

    public Object getSource() {
        return source;
    }

    public String getScript() {
        return script;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return !started;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ExecutionEvent that = (ExecutionEvent) o;

        if (started != that.started) return false;
        if (!source.equals(that.source)) return false;
        if (!script.equals(that.script)) return false;
        return result != null ? result.equals(that.result) : that.result == null;
    }

    @Override
    public int hashCode() {
        int result1 = source.hashCode();
        result1 = 31 * result1 + script.hashCode();
        result1 = 31 * result1 + (started ? 1 : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "ExecutionEvent{" +
            "source=" + source +
            ", script='" + script + '\'' +
            ", started=" + started +
            ", result='" + result + '\'' +
            '}';
    }
}
