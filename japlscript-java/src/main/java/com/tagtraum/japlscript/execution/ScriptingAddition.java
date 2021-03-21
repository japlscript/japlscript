/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ScriptingAddition.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ScriptingAddition {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptingAddition.class);
    public enum Architecture { I368, PPC, UNIVERSAL, UNKNOWN }

    private java.io.File executable;
    private java.io.File folder;
    private Architecture architecture;

    public ScriptingAddition(final java.io.File file) {
        if (isScriptingAdditionFolder(file)) {
            this.folder = file;
            this.architecture = Architecture.UNKNOWN;
        } else {
            this.executable = file;
            this.folder = file;
            while (folder != null && !isScriptingAdditionFolder(folder)) {
                folder = folder.getParentFile();
            }
            this.architecture = determineArchitecture();
        }
    }

    public java.io.File getExecutable() {
        return executable;
    }

    public File getFolder() {
        return folder;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    /**
     *
     * @return true if this Scripting Addition is suitable for the local architecture
     */
    public boolean isLocalArchitecture() {
        return architecture == Architecture.UNIVERSAL
                || System.getProperty("os.arch").equalsIgnoreCase(architecture.toString());
    }

    private Architecture determineArchitecture() {
        if (executable != null) {
            final ProcessBuilder processBuilder = new ProcessBuilder("file", "-b", executable.toString());
            final StringBuilder sb = new StringBuilder();
            processBuilder.redirectErrorStream(true);
            try {
                final Process process = processBuilder.start();
                final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                for (String line=in.readLine(); line != null; line=in.readLine()) {
                    sb.append(line);
                }
            } catch (IOException e) {
                LOG.error(e.toString(), e);
            }
            final String s = sb.toString();
            if (s.contains("i386") && s.contains("ppc")) return Architecture.UNIVERSAL;
            else if (s.contains("i386")) return Architecture.I368;
            else if (s.contains("ppc")) return Architecture.PPC;
        }
        return Architecture.UNKNOWN;
    }


    private static boolean isScriptingAdditionFolder(final java.io.File file) {
        return file.isDirectory() && file.getName().endsWith(".osax");
    }

    @Override
    public String toString() {
        if (folder != null) return "[" + architecture + " binary]: " + folder;
        return "[" + architecture + " binary]: " + executable;
    }

}
