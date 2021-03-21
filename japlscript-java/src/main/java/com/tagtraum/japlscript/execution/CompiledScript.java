/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import com.tagtraum.japlscript.JaplScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Compiled script.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class CompiledScript {

    private static final Logger LOG = LoggerFactory.getLogger(CompiledScript.class);
    private CharSequence script;
    private String scriptFile;

    public CompiledScript(final CharSequence script, final String scriptFile) {
        this.script = script;
        this.scriptFile = scriptFile;
    }

    public CharSequence getScript() {
        return script;
    }

    /**
     * Executes the script.
     *
     * @return compiled script
     * @throws IOException in case of IO problems
     */
    public String execute() throws IOException {
        final Process process = Runtime.getRuntime().exec(new String[]{"osascript", scriptFile});
        final ReaderPump stderr = new ReaderPump(new InputStreamReader(process.getErrorStream(), UTF_8));
        final ReaderPump stdout = new ReaderPump(new InputStreamReader(process.getInputStream(), UTF_8));
        // TODO: investigate ThreadPool use
        final Thread errThread = new Thread(stderr);
        final Thread outThread = new Thread(stdout);
        errThread.start();
        outThread.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e.toString());
        }
        try {
            errThread.join();
            outThread.join();
        } catch (InterruptedException e) {
            final IOException ioe = new IOException(e.toString());
            ioe.initCause(e);
            throw ioe;
        }
        if (LOG.isDebugEnabled()) LOG.debug("Exit value  : " + process.exitValue());
        if (LOG.isDebugEnabled()) LOG.debug("Return value: " + stdout.getValue());
        if (stderr.getIOException() != null) throw stderr.getIOException();
        if (stdout.getIOException() != null) throw stdout.getIOException();
        if (stderr.getValue().length() > 0) throw new JaplScriptException(stderr.getValue(), script.toString());
        return stdout.getValue();
    }

    private static class ReaderPump implements Runnable {

        private Reader in;
        private String value;
        private IOException ioException;
        private static final int ONE_KB = 1024;

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
            int count = 0;
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
}
