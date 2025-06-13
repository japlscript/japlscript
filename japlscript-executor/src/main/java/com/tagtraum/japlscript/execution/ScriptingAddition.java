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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Scripting addition.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ScriptingAddition {

    private static final Logger LOG = Logger.getLogger(ScriptingAddition.class.getName());
    public enum Architecture { I368, PPC, X86_64, AARCH64, UNKNOWN }

    private final java.io.File executable;
    private final java.io.File folder;
    private final Set<Architecture> architectures;

    public ScriptingAddition(final java.io.File file) {
        if (isScriptingAdditionFolder(file)) {
            this.folder = file;
            this.architectures = Set.of(Architecture.UNKNOWN);
            this.executable = null;
        } else {
            this.executable = file;
            java.io.File f = file;
            while (f != null && !isScriptingAdditionFolder(f)) {
                f = f.getParentFile();
            }
            this.folder = f;
            this.architectures = determineArchitectures();
        }
    }

    public java.io.File getExecutable() {
        return executable;
    }

    public File getFolder() {
        return folder;
    }

    public Set<Architecture> getArchitectures() {
        return architectures;
    }

    /**
     *
     * @return true if this Scripting Addition is suitable for the local architecture
     */
    public boolean isLocalArchitecture() {
        // this may or may not work so well...
        return architectures.toString().toLowerCase().contains(System.getProperty("os.arch").toLowerCase());
    }

    private Set<Architecture> determineArchitectures() {
        final Set<Architecture> architectures = new HashSet<>();
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
            if (s.contains("i386")) architectures.add(Architecture.I368);
            if (s.contains("ppc")) architectures.add(Architecture.PPC);
            if (s.contains("x86_64")) architectures.add(Architecture.X86_64);
            if (s.contains("arm64") || s.contains("aarch64")) architectures.add(Architecture.AARCH64);
        }
        if (architectures.isEmpty()) {
            architectures.add(Architecture.UNKNOWN);
        }
        return architectures;
    }


    private static boolean isScriptingAdditionFolder(final java.io.File file) {
        return file.isDirectory() && file.getName().endsWith(".osax");
    }

    @Override
    public String toString() {
        if (folder != null) return "[" + architectures + " binary]: " + folder;
        return "[" + String.join(", ", architectures.stream().map(Enum::toString).collect(Collectors.joining(","))) + " binary]: " + executable;
    }

}
