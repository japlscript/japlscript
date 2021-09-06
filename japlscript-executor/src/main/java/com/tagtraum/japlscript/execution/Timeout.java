/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

/**
 * Timeout aspect.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Timeout implements Aspect {

    private final int seconds;

    public Timeout(final int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public String before(final String application, final String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("with timeout of ").append(seconds).append(" second");
        if (seconds != 1) sb.append('s');
        return sb.toString();
    }

    @Override
    public String after(final String application, final String body) {
        return "end timeout";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Timeout timeout = (Timeout) o;

        if (seconds != timeout.seconds) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return seconds;
    }

    @Override
    public String toString() {
        return "Timeout{" +
                "seconds=" + seconds +
                '}';
    }
}
