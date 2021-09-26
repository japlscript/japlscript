/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;

/**
 * Ant generator task. This is mostly a facade for the actual {@link Generator}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class GeneratorAntTask extends Task {

    private final Generator generator;

    public GeneratorAntTask() {
        this.generator = new Generator();
        // setup dumb log redirection
        this.generator.setLogMessageConsumer((message, level) -> {
            switch (level.intValue()) {
                case 1000:
                    log(message, Project.MSG_ERR);
                    break;
                case 900:
                    log(message, Project.MSG_WARN);
                    break;
                case 800:
                    log(message, Project.MSG_INFO);
                    break;
                case 500:
                    log(message, Project.MSG_DEBUG);
                    break;
                default:
                    log(message);
            }
        });
    }

    @Override
    public void execute() {
        try {
            generator.generate();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * Lets you configure a custom mapping from AppleScript types
     * to Java types.
     *
     * @param typeMapping type mapping
     */
    public void addConfiguredTypeMapping(final TypeMapping typeMapping) {
        generator.addConfiguredTypeMapping(typeMapping);
    }

    /**
     *
     * @param excludeClass excluded class
     */
    public void addConfiguredExcludeClass(final ExcludeClass excludeClass) {
        generator.addConfiguredExcludeClass(excludeClass);
    }

    public void setSdef(final File sdef) {
        generator.setSdef(sdef);
    }

    /*
    public void setSdef(final Path sdef) {
        generator.setSdef(sdef);
    }
    */

    /**
     *
     * @param packagePrefix prefix for generated package names
     */
    public void setPackagePrefix(final String packagePrefix) {
        generator.setPackagePrefix(packagePrefix);
    }

    /**
     * Application name or bundle that would be used in an AppleScript call.
     * E.g. "iTunes".
     *
     * @param application application name
     */
    public void setApplication(final String application) {
        generator.setApplication(application);
    }

    /**
     * Name of the generated JPMS module.
     *
     * @param module module name name
     */
    public void setModule(final String module) {
        generator.setModule(module);
    }

    public void setOut(final File out) {
        generator.setOut(out);
    }

    public boolean isScriptingAddition() {
        return generator.isScriptingAddition();
    }

    /**
     * Indicate whether the generated code is for a scripting addition instead of
     * an application.
     *
     * @param scriptingAddition true or false
     */
    public void setScriptingAddition(final boolean scriptingAddition) {
        this.generator.setScriptingAddition(scriptingAddition);
    }

    /*
    public void setOut(final Path out) {
        generator.setOut(out);
    }
    */

}
