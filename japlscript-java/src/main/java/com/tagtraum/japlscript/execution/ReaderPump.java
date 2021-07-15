/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * Pump that continuously reads from a {@link Reader}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ReaderPump implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderPump.class);
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
            if (LOG.isDebugEnabled()) LOG.debug(ioe.toString(), ioe);
            this.value = ioe.toString();
            this.ioException = ioe;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) LOG.debug(e.toString(), e);
            }
        }
    }
}
