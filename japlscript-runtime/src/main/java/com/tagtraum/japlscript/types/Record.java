/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

/**
 * Record - this is just a placeholder. Records aren't fully supported just yet.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Record extends ReferenceImpl {

    // TODO: this is just a placeholder
    private static final Record instance = new Record();

    private Record() {
        super(null, null);
    }

    public static Record getInstance() {
        return instance;
    }

    /**
     *
     * @param objectReference object reference
     * @param applicationReference application reference
     */
    public Record(final String objectReference, final String applicationReference) {
        super(objectReference, applicationReference);
    }

    @Override
    public String toString() {
        return getObjectReference();
    }

}
