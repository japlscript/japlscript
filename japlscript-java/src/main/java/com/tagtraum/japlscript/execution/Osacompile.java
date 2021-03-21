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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Osacompile.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Osacompile {
    private static final Logger LOG = LoggerFactory.getLogger(Osacompile.class);
    private static final Path JAPLSCRIPT_CACHE_DIRECTORY = new File(System.getProperty("user.home") + "/Library/Caches/JaplScript/").toPath();
    static {
        try {
            Files.createDirectories(JAPLSCRIPT_CACHE_DIRECTORY);
        } catch (IOException e) {
            LOG.error(e.toString(), e);
        }
    }
    private final Map<CharSequence, CompiledScript> COMPILED_SCRIPTS = new HashMap<>();

    /**
     * Compiles the script.
     *
     * @param script script
     * @return compiled script
     * @throws IOException in case of IO issues
     */
    public CompiledScript compile(final CharSequence script) throws IOException {
        CompiledScript compiledScript = getCachedCompiledScript(script);
        if (compiledScript != null) {
            if (LOG.isDebugEnabled()) LOG.debug("Script is already compiled.");
            return compiledScript;
        } else {
            if (LOG.isDebugEnabled()) LOG.debug("Script needs to be compiled.");
            final Path scriptFile = Files.createTempFile(JAPLSCRIPT_CACHE_DIRECTORY, "japlscript", ".scpt");
            // TODO: deleteOnExit() is evil! remove this somehow
            scriptFile.toFile().deleteOnExit();
            final Process process = Runtime.getRuntime().exec(new String[]{"osacompile", "-o", scriptFile.toString()});
            final Writer stdin = new OutputStreamWriter(process.getOutputStream(), "MacRoman");
            stdin.write(script.toString());
            stdin.close();
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
            if (stderr.getIOException() != null) throw stderr.getIOException();
            if (stdout.getIOException() != null) throw stdout.getIOException();
            if (stderr.getValue().length() > 0) throw new JaplScriptException(stderr.getValue(), script.toString());
            compiledScript = new CompiledScript(script, scriptFile.toString());
            cacheCompiledScript(compiledScript);
            return compiledScript;
        }
    }

    private synchronized CompiledScript getCachedCompiledScript(final CharSequence script) {
        return COMPILED_SCRIPTS.get(script);
    }

    private synchronized CompiledScript cacheCompiledScript(final CompiledScript compiledScript) {
        return COMPILED_SCRIPTS.put(compiledScript.getScript(), compiledScript);
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
