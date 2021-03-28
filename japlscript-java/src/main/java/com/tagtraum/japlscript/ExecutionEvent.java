/*
 * =================================================
 * Copyright 2020 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

/**
 * ExecutionEvent.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ExecutionEvent {
    private final Object source;
    private final String script;
    private final boolean started;
    private final String result;

    public ExecutionEvent(final Object source, final String script, final boolean started, final String result) {
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
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (script != null ? !script.equals(that.script) : that.script != null) return false;
        return result != null ? result.equals(that.result) : that.result == null;
    }

    @Override
    public int hashCode() {
        int hash = source != null ? source.hashCode() : 0;
        hash = 31 * hash + (script != null ? script.hashCode() : 0);
        hash = 31 * hash + (started ? 1 : 0);
        hash = 31 * hash + (result != null ? result.hashCode() : 0);
        return hash;
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
