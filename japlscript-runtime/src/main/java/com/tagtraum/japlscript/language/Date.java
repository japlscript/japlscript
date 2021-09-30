/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.language;

import com.tagtraum.japlscript.Chevron;
import com.tagtraum.japlscript.DateParser;
import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.japlscript.Codec;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Date implements Codec<java.util.Date> {

    private static final Date instance = new Date();
    private static final TypeClass[] CLASSES = {
        new TypeClass("date", new Chevron("class", "ldt "))
    };

    private Date() {
    }

    /**
     * Null instance used for {@link Codec} implementation.
     *
     * @return null instance
     */
    public static Date getInstance() {
        return instance;
    }

    @Override
    public String _encode(final Object date) {
        if (date == null) return "null"; // or "missing value"?
        final SimpleDateFormat dateHelperFormat = new SimpleDateFormat("'my createDate('yyyy, M, d, H, m, s')'");
        return dateHelperFormat.format(date);
    }

    public java.util.Date _decode(final String objectReference, final String applicationReference) {
        if (objectReference == null) return null;
        return parseDate(objectReference);
    }

    private static java.util.Date parseDate(final String objectReference) {
        // our best bet, as it is produced by CocoaScriptExecutor
        final DateFormat rfc3339Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        rfc3339Format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return rfc3339Format.parse(objectReference);
        } catch (ParseException e) {
            // ignore
        }
        // this is needed for Osascript
        final int firstQuote = objectReference.indexOf('\"');
        final int lastQuote = objectReference.lastIndexOf('\"');
        if (firstQuote < 0 || lastQuote < 0) {
            throw new JaplScriptException("Failed to parse date: " + objectReference);
        }
        final String d = objectReference.substring(firstQuote+1, lastQuote);
        try {
            return new DateParser(Locale.getDefault()).parse(d);
        } catch (ParseException e) {
            // ignore
        }
        try {
            return new DateParser(Locale.US).parse(d);
        } catch (ParseException e) {
            // ignore
        }
        // try standard US formats
        for (int format = DateFormat.FULL; format<=DateFormat.SHORT; format++) {
            try {
                return DateFormat.getDateTimeInstance(format, format, Locale.US).parse(d);
            } catch (Exception e) {
                // ignore
            }
        }
        // try standard local formats
        for (int format=DateFormat.FULL; format<=DateFormat.SHORT; format++) {
            try {
                return DateFormat.getDateTimeInstance(format, format, Locale.getDefault()).parse(d);
            } catch (Exception e) {
                // ignore
            }
        }
        throw new JaplScriptException("Failed to parse date: " + objectReference);
    }

    @Override
    public Class<java.util.Date> _getJavaType() {
        return java.util.Date.class;
    }

    @Override
    public TypeClass[] _getAppleScriptTypes() {
        return CLASSES;
    }
}
