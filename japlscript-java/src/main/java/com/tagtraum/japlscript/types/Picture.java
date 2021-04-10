/*
 * =================================================
 * Copyright 2007 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Picture.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Picture extends ReferenceImpl {

    private byte[] data;
    private String format;

    /**
     *
     * @param objectReference object reference
     * @param applicationReference application reference
     */
    public Picture(final String objectReference, final String applicationReference) {
        super(objectReference, applicationReference);
        // TODO: somehow reformat the data stuff and put it into the byte array
        // probably skip <<data, then read hex and then skip >>
        if (objectReference.startsWith("data ", 1)) {
            // get format as hex code
            format = objectReference.substring("<data ".length(), "<data ".length() + 4);
            // skip the class info (first four hex chars)
            final String hexString = objectReference.substring("<data ".length() + 4, objectReference.length()-1);
            final ByteArrayOutputStream out = new ByteArrayOutputStream(hexString.length() / 2);
            for (int i=0; i<hexString.length(); i+=2) {
                out.write(Integer.parseInt(hexString.substring(i, i+2), 16));
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

    public Image getImage() throws IOException {
        if (data == null) return null;
        // try imageIO first
        Image image = ImageIO.read(new ByteArrayInputStream(data));
        // fall back to QuickTime
        /*
        if (image == null && format != null) {
            image = createImageWithQT(data, format);
        }
        */
        return image;
    }

    public String getFormat() {
        return format;
    }

}
