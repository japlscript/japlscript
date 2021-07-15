/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import com.tagtraum.japlscript.JaplScriptException;
import com.tagtraum.japlscript.ScriptExecutor;
import com.tagtraum.japlscript.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Osascript.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Osascript extends ScriptExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(Osascript.class);
    private static final Osacompile osacompile = new Osacompile();
    private static final int NO_ERRORS = 0;
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public Osascript() {
    }

    /**
     * Execute script.
     *
     * @return return value
     */
    public String executeImpl() throws IOException {
        final Session session = Session.getSession();
        if (session != null && session.isCompile()) {
            if (LOG.isDebugEnabled()) LOG.debug("Using compiled script.");
            final CompiledScript compiledScript = osacompile.compile(getScript());
            if (compiledScript != null) return compiledScript.execute();
        }
        final String[] cmdarray = new String[]{"osascript", "-s", "s", "-"};
        final Process process = Runtime.getRuntime().exec(cmdarray);
        final Writer stdin = new OutputStreamWriter(process.getOutputStream(), "MacRoman");
        stdin.write(getScript().toString());
        stdin.close();
        final ReaderPump stderr = new ReaderPump(new InputStreamReader(process.getErrorStream(), UTF_8));
        final ReaderPump stdout = new ReaderPump(new InputStreamReader(process.getInputStream(), UTF_8));
        final Future<?> outFuture = THREAD_POOL.submit(stdout);
        final Future<?> errFuture = THREAD_POOL.submit(stderr);
        final int exit;
        try {
            exit = process.waitFor();
            if (LOG.isDebugEnabled()) LOG.debug("Exit value: " + exit);
        } catch (InterruptedException e) {
            throw new IOException(e.toString());
        }
        try {
            errFuture.get();
            outFuture.get();
        } catch (InterruptedException e) {
            throw new IOException(e.toString(), e);
        } catch (ExecutionException e) {
            LOG.error(e.toString(), e);
            throw new IOException(e.toString(), e.getCause());
        }
        if (LOG.isDebugEnabled() && stdout.getValue() != null && stdout.getValue().length() > 0) {
            final String returnValue = stdout.getValue();
            LOG.debug("Return value: " + returnValue.substring(0, Math.min(MAX_RETURNVALUE_LOG_LENGTH,
                    returnValue.length())));
        }
        if (stderr.getIOException() != null) throw stderr.getIOException();
        if (stdout.getIOException() != null) throw stdout.getIOException();
        if (exit != NO_ERRORS) {
            final String stderrValue = stderr.getValue();
            if (stderrValue.length() > 0) {
                if (BadScriptingAdditionException.isBadScriptingAdditionMessage(stderrValue)) {
                    throw new BadScriptingAdditionException(stderrValue);
                } else {
                    throw new JaplScriptException(stderrValue, getScript().toString());
                }
            }
            else throw new JaplScriptException("Unknown Error", getScript().toString());
        }
        return stdout.getValue();
    }
}
