/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pump that continuously reads from a {@link Reader}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ReaderPump implements Runnable {

    private static final Logger LOG = Logger.getLogger(ReaderPump.class.getName());
    private static final int ONE_KB = 1024;
    private final Reader in;
    private String value;
    private IOException ioException;

    public ReaderPump(final Reader in) {
        this.in = in;
    }

    public String getValue() {
        return this.value;
    }

    public IOException getIOException() {
        return ioException;
    }

    @Override
    public void run() {
        final char[] cbuf = new char[ONE_KB];
        final StringBuilder sb = new StringBuilder();
        int count;
        try {
            while ((count = in.read(cbuf)) != -1) {
                sb.append(cbuf, 0, count);
            }
            this.value = sb.toString().trim();
        } catch (IOException ioe) {
            if (LOG.isLoggable(Level.FINE)) LOG.log(Level.FINE, ioe.toString(), ioe);
            this.value = ioe.toString();
            this.ioException = ioe;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                if (LOG.isLoggable(Level.FINE)) LOG.log(Level.FINE, e.toString(), e);
            }
        }
    }
}
