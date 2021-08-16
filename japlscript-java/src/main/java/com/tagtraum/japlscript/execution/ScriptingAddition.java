/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ScriptingAddition.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ScriptingAddition {

    private static final Logger LOG = Logger.getLogger(ScriptingAddition.class.getName());
    public enum Architecture { I368, PPC, X86_64, AARCH64, UNIVERSAL, UNKNOWN }

    private final java.io.File executable;
    private final java.io.File folder;
    private final Architecture architecture;

    public ScriptingAddition(final java.io.File file) {
        if (isScriptingAdditionFolder(file)) {
            this.folder = file;
            this.architecture = Architecture.UNKNOWN;
            this.executable = null;
        } else {
            this.executable = file;
            java.io.File f = file;
            while (f != null && !isScriptingAdditionFolder(f)) {
                f = f.getParentFile();
            }
            this.folder = f;
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
                LOG.log(Level.SEVERE, e.toString(), e);
            }
            final String s = sb.toString();
            if (s.contains("i386") && s.contains("ppc")) return Architecture.UNIVERSAL;
            else if (s.contains("i386")) return Architecture.I368;
            else if (s.contains("ppc")) return Architecture.PPC;
            else if (s.contains("x86_64")) return Architecture.X86_64;
            else if (s.contains("arm64") || s.contains("aarch64")) return Architecture.AARCH64;
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
