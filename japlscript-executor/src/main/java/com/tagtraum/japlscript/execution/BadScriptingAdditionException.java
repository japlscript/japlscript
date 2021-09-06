/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Bad scripting addition exception.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class BadScriptingAdditionException extends JaplScriptException {

    private static final String DYLD_RETURNS_2_WHEN_TRYING_TO_LOAD = "dyld returns 2 when trying to load";
    private List<ScriptingAddition> offendingScriptingAdditions;


    public BadScriptingAdditionException(final String errorMessage) {
        super(errorMessage);
        parseErrorMessage(errorMessage);
    }

    public static boolean isBadScriptingAdditionMessage(final String errorMessage) {
        return errorMessage.contains(DYLD_RETURNS_2_WHEN_TRYING_TO_LOAD);
    }

    private void parseErrorMessage(final String stderr) {
        final BufferedReader in = new BufferedReader(new StringReader(stderr));
        offendingScriptingAdditions = new ArrayList<ScriptingAddition>();
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if (line.contains(DYLD_RETURNS_2_WHEN_TRYING_TO_LOAD)) {
                    final String fileName = line.substring(line.indexOf(DYLD_RETURNS_2_WHEN_TRYING_TO_LOAD)
                            + DYLD_RETURNS_2_WHEN_TRYING_TO_LOAD.length() + 1).trim();
                    offendingScriptingAdditions.add(new ScriptingAddition(new java.io.File(fileName)));
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    public List<ScriptingAddition> getOffendingScriptingAdditions() {
        return offendingScriptingAdditions;
    }
}
