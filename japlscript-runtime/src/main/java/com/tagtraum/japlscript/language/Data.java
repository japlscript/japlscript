/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.Chevron;
import com.tagtraum.japlscript.Codec;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.ByteArrayOutputStream;

/**
 * Data.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Data extends ReferenceImpl {

    private static final Logger LOG = Logger.getLogger(Data.class.getName());
    private static final Data instance = new Data();
    private static final TypeClass[] CLASSES = {
        new TypeClass("data", new Chevron("class", "rdat"))
    };
    private byte[] data;

    private Data() {
        super(null, null);
    }

    /**
     *
     * @param objectReference object reference
     * @param applicationReference application reference
     */
    public Data(final String objectReference, final String applicationReference) {
        super(objectReference, applicationReference);
        // TODO: somehow reformat the data stuff and put it into the byte array
        // probably skip <<data, then read hex and then skip >>
        if (objectReference.startsWith("data ", 1)) {
            final String hexString = objectReference.substring("<data ".length(), objectReference.length()-1);
            final ByteArrayOutputStream out = new ByteArrayOutputStream(hexString.length() / 2);
            for (int i=0; i<hexString.length(); i+=2) {
                try {
                    out.write(java.lang.Integer.parseInt(hexString.substring(i, i+2), 16));
                } catch (NumberFormatException e) {
                    LOG.log(Level.SEVERE, e.toString(), e);
                }
            }
            data = out.toByteArray();
        }
    }

    /**
     * Null instance used for {@link Codec} implementation.
     *
     * @return null instance
     */
    public static Data getInstance() {
        return instance;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }
}
