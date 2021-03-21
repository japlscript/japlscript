/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;

/**
 * Data.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Data extends ReferenceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(Data.class);
    private byte[] data;

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
                    out.write(Integer.parseInt(hexString.substring(i, i+2), 16));
                } catch (NumberFormatException e) {
                    LOG.error(e.toString(), e);
                }
            }
            data = out.toByteArray();
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }
}
