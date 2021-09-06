/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Compiled script.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class CompiledScript {

    private static final Logger LOG = Logger.getLogger(CompiledScript.class.getName());
    private final CharSequence script;
    private final String scriptFile;

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
            throw new IOException(e.toString(), e);
        }
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Exit value  : " + process.exitValue());
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Return value: " + stdout.getValue());
        if (stderr.getIOException() != null) throw stderr.getIOException();
        if (stdout.getIOException() != null) throw stdout.getIOException();
        if (stderr.getValue().length() > 0) throw new JaplScriptException(stderr.getValue(), script.toString());
        return stdout.getValue();
    }

}
