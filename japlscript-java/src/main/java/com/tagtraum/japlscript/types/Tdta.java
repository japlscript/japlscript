/*
 * =================================================
 * Copyright 2008 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Type Data.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Tdta extends ReferenceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(Tdta.class);
    private byte[] tdta = new byte[0];
    private static final int DIM = 55;
    private static final int[][] HEXMAP = new int[DIM][];

    static {
        //0 = 48
        //9 = 57 - 48 = 9 
        //A = 65 - 48 = 17
        //F = 70 - 48 = 22
        //a = 97 - 48 = 49
        //f = 102 - 48 = 54
        // fast char-pair to int conversion
        for (int i=0 ; i<10; i++) {
            HEXMAP[i] = new int[DIM];
            for (int j=0; j<10; j++) {
                HEXMAP[i][j] = Integer.parseInt("" + i + "" + j, 16);
            }
            for (char j='A'; j<'G'; j++) {
                HEXMAP[i][j-'0'] = Integer.parseInt("" + i + "" + Character.toString(j), 16);
            }
            for (char j='a'; j<'g'; j++) {
                HEXMAP[i][j-'0'] = Integer.parseInt("" + i + "" + Character.toString(j), 16);
            }
        }
        for (char i='A' ; i<'G'; i++) {
            HEXMAP[i-'0'] = new int[DIM];
            for (int j=0; j<10; j++) {
                HEXMAP[i-'0'][j] = Integer.parseInt(Character.toString(i) + j, 16);
            }
            for (char j='A'; j<'G'; j++) {
                HEXMAP[i-'0'][j-'0'] = Integer.parseInt( Character.toString(i) + Character.toString(j), 16);
            }
            for (char j='a'; j<'g'; j++) {
                HEXMAP[i-'0'][j-'0'] = Integer.parseInt(Character.toString(i) + Character.toString(j), 16);
            }
        }
        for (char i='a' ; i<'g'; i++) {
            HEXMAP[i-'0'] = new int[DIM];
            for (int j=0; j<10; j++) {
                HEXMAP[i-'0'][j] = Integer.parseInt(Character.toString(i) + j, 16);
            }
            for (char j='A'; j<'G'; j++) {
                HEXMAP[i-'0'][j-'0'] = Integer.parseInt( Character.toString(i) + Character.toString(j), 16);
            }
            for (char j='a'; j<'g'; j++) {
                HEXMAP[i-'0'][j-'0'] = Integer.parseInt(Character.toString(i) + Character.toString(j), 16);
            }
        }
    }

    private static int hexToInt(final char[] array, final int offset) {
        return HEXMAP[array[offset]-'0'][array[offset+1]-'0'];
    }

    public Tdta(final byte[] buf, final String applicationReference) {
        super("\u00abdata tdta" + toHex(buf) + "\u00bb", applicationReference);
        this.tdta = buf;
    }

    public Tdta(final File file, final String applicationReference) throws IOException {
        this("\u00abdata tdta" + toHex(file) + "\u00bb", applicationReference);
    }

    private static String toHex(final byte[] buf) {
        final StringBuilder sb = new StringBuilder(buf.length*2);
        for (final byte b : buf) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static String toHex(final File file) throws IOException {
        final StringBuilder sb = new StringBuilder((int)file.length()*2);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            final byte[] buf = new byte[64*1024];
            int justRead;
            while ((justRead = in.read(buf)) > 0) {
                for (int i=0; i<justRead; i++) {
                    sb.append(String.format("%02X", buf[i]));
                }
            }
            return sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error(e.toString(), e);
                }
            }
        }
    }

    /**
     *
     * @param objectReference object reference
     * @param applicationReference application reference
     */
    public Tdta(final String objectReference, final String applicationReference) {
        super(objectReference, applicationReference);
        // TODO: somehow reformat the tdta stuff and put it into the byte array
        // probably skip <<data tdta, then read hex and then skip >>
        if (objectReference.startsWith("data tdta", 1)) {
            final String hexString = objectReference.substring("<data tdta".length(), objectReference.length()-1);
            final ByteArrayOutputStream out = new ByteArrayOutputStream(hexString.length() / 2);
            /*
            for (int i=0; i<hexString.length(); i+=2) {
                try {
                    out.write(Integer.parseInt(hexString.substring(i, i+2), 16));
                } catch (NumberFormatException e) {
                    LOG.error(e.toString(), e);
                }
            }
            */
            final char[] chars = hexString.toCharArray();
            for (int i=0; i<hexString.length(); i+=2) {
                try {
                    out.write(hexToInt(chars, i));
                } catch (NumberFormatException e) {
                    LOG.error(e.toString(), e);
                }
            }
            tdta = out.toByteArray();
        }
    }

    public byte[] getTdta() {
        return tdta;
    }

    public void setTdta(final byte[] tdta) {
        this.tdta = tdta;
    }
}
