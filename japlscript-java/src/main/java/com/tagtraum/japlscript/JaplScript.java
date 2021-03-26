/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public final class JaplScript {

    private static final Logger LOG = LoggerFactory.getLogger(JaplScript.class);
    private static final int LAST_ASCII_CHAR = 127;
    private static final List<Aspect> globalAspects = new ArrayList<>();
    static {
        globalAspects.add(new DateHelper());
        globalAspects.add(new Tell());
    }

    private JaplScript() {
    }

    public static void addGlobalAspect(final Aspect aspect) {
        globalAspects.add(aspect);
    }

    public static boolean removeGlobalAspect(final Aspect aspect) {
        return globalAspects.remove(aspect);
    }

    /**
     * @return copy of the aspect list
     */
    public static List<Aspect> getGlobalAspects() {
        return new ArrayList<>(globalAspects);
    }


    /**
     * Starts a session.
     *
     * @return thread specific session
     */
    public static Session startSession() {
        Session session = Session.getSession();
        if (session == null) {
            session = new Session();
        }
        return session;
    }

    /**
     * Gets the application object for an application.
     *
     * @param interfaceClass interface class
     * @param applicationName application name, e.g. "iTunes" or "Music"
     * @param <T> t
     * @return application object
     */
    public static <T> T getApplication(final java.lang.Class<T> interfaceClass, final String applicationName) {
        final Reference reference = new ReferenceImpl(null, "application \"" + applicationName + "\"");
        return cast(interfaceClass, reference);
    }

    /**
     *
     * @param interfaceClass interface class
     * @param reference reference to cast to interface class
     * @param <T> target type
     * @return object of type T
     */
    public static <T> T cast(final java.lang.Class<T> interfaceClass, final Reference reference) {
        if (reference == null) return null;
        try {
            final String objectReference = reference.getObjectReference();
            final String applicationReference = reference.getApplicationReference();

            if (interfaceClass == String.class) {
                String trimmed = objectReference;
                if (trimmed != null && trimmed.startsWith("\"") && trimmed.endsWith("\""))
                    trimmed = trimmed.substring(1, trimmed.length() - 1);
                return (T) trimmed;
            } else if (interfaceClass == Integer.TYPE) {
                return (T) (Integer) Integer.parseInt(objectReference);
            } else if (interfaceClass == Boolean.TYPE) {
                return (T) (Boolean) Boolean.parseBoolean(objectReference);
            } else if (interfaceClass == Float.TYPE) {
                return (T) (Float) Float.parseFloat(objectReference);
            } else if (interfaceClass == Double.TYPE) {
                return (T) (Double) Double.parseDouble(objectReference);
            } else if (interfaceClass == Long.TYPE) {
                return (T) (Long) Long.parseLong(objectReference);
            } else if (interfaceClass == Date.class) {
                if (objectReference == null) return null;
                return (T)parseDate(objectReference);
            } else if (interfaceClass == Alias.class) {
                if (objectReference == null) return null;
                return (T) new Alias(objectReference, applicationReference);
            } else if (interfaceClass == Data.class) {
                if (objectReference == null) return null;
                return (T) new Data(objectReference, applicationReference);
            } else if (interfaceClass == Picture.class) {
                if (objectReference == null) return null;
                return (T) new Picture(objectReference, applicationReference);
            } else if (interfaceClass == Tdta.class) {
                if (objectReference == null) return null;
                return (T) new Tdta(objectReference, applicationReference);
            } else if (interfaceClass == JaplScriptFile.class) {
                if (objectReference == null) return null;
                return (T) new JaplScriptFile(objectReference, applicationReference);
            } else if (interfaceClass == TypeClass.class) {
                if (objectReference == null) return null;
                return (T) new TypeClass(objectReference, applicationReference);
            } else if (interfaceClass.isArray()) {
                if (objectReference == null) return null;
                return (T) parseList(interfaceClass.getComponentType(), reference);
            }
            // TODO: add more primitive/standard types from Types class
            if (objectReference != null && objectReference.trim().length() == 0) {
                return null;
            }
            if (JaplEnum.class.isAssignableFrom(interfaceClass)) {
                try {
                    final Method getMethod = interfaceClass.getMethod("get", String.class);
                    return (T) getMethod.invoke(null, objectReference);
                } catch (Exception e) {
                    throw new JaplScriptException(e);
                }
            }
            if (!interfaceClass.isInterface()) {
                if (objectReference != null) {
                    throw new JaplScriptException("Cannot create proxy for non-interface class " + interfaceClass);
                }
                else {
                    LOG.warn("Attempt to cast " + reference + " to unregistered class type " + interfaceClass
                            + ". As the object reference is null, we simply default to also returning null. Consider adding "
                            + interfaceClass + " to known cast types.");
                    return null;
                }
            }
            return (T) Proxy.newProxyInstance(JaplScript.class.getClassLoader(),
                    new Class[]{interfaceClass}, new ObjectInvocationHandler(reference));
        } catch (JaplScriptException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new JaplScriptException("Failed to cast " + reference + " to " + interfaceClass);
        }
    }

    private static Date parseDate(final String objectReference) {
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

    private static <T> T[] parseList(final java.lang.Class<T> interfaceClass, final Reference reference) {
        final String objectReference = reference.getObjectReference();
        final String applicationReference = reference.getApplicationReference();
        //if (LOG.isDebugEnabled()) LOG.debug("objectReference: " + objectReference);
        //if (LOG.isDebugEnabled()) LOG.debug("applicationReference: " + applicationReference);
        //if (LOG.isDebugEnabled()) LOG.debug("interfaceClass: " + interfaceClass);
        final List<T> result = new ArrayList<>();
        int depth = 0;
        final boolean curlies = objectReference.startsWith("{");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objectReference.length(); i++) {
            final char c = objectReference.charAt(i);
            switch (c) {
                case '{':
                    depth++;
                    break;
                case '}':
                    depth--;
                    break;
                default:
            }
            final boolean lastChar = i == objectReference.length() - 1;
            if (depth == 1 && c == ',' || depth == 0 && c == '}' || !curlies && (c == ',' || lastChar)) {
                if (!curlies && lastChar) sb.append(c);
                if (sb.length() > 0) {
                    //if (LOG.isDebugEnabled()) LOG.debug("arr ref: " + sb);
                    result.add(cast(interfaceClass, new ReferenceImpl(sb.toString(), applicationReference)));
                    sb.setLength(0);
                }
            } else if (depth == 1 && c != '{') sb.append(c);
            else if (!curlies) sb.append(c);
            else if (depth > 1) sb.append(c);
        }
        return result.toArray((T[]) Array.newInstance(interfaceClass, result.size()));
    }

    /**
     * Escapes the given String to a unicode String in the AppleScript sense.
     *
     * @param s string
     * @return unicode rep for the string
     */
    public static String asUnicodeText(final String s) {
        final StringBuilder sb = new StringBuilder();
        sb.append("(\u00abdata utf8");
        final byte[] buf = s.getBytes(UTF_8);
        for (byte b : buf) {
            final int i = (int) b & 0xff;
            final String hex = Integer.toHexString(i);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        sb.append("\u00bb as Unicode text)");
        return sb.toString();
    }

    /**
     * Quotes the string to make it usable for AppleScript.
     *
     * @param s string
     * @return quoted string
     */
    public static String quote(final String s) {
        final StringBuilder sb = new StringBuilder();
        sb.append("(\"");
        int unicodeStart = -1;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c > LAST_ASCII_CHAR) {
                if (unicodeStart == -1) unicodeStart = i;
            } else {
                if (unicodeStart != -1) {
                    sb.append("\" & ");
                    sb.append(asUnicodeText(s.substring(unicodeStart, i)));
                    sb.append(" & \"");
                    unicodeStart = -1;
                }
                switch (c) {
                    case '"':
                        sb.append('\\');
                    default:
                        sb.append(c);
                }
            }
        }
        if (unicodeStart != -1) {
            sb.append("\" & ");
            sb.append(asUnicodeText(s.substring(unicodeStart)));
            sb.append(")");
        } else {
            sb.append("\")");
        }
        return sb.toString();
    }

    private static class Tell implements Aspect {

        @Override
        public String before(final String application, final String body) {
            if (application != null)
                return "tell " + application;
            else
                return "";
        }

        @Override
        public String after(final String application, final String body) {
            if (application != null)
                return "end tell";
            else
                return "";
        }
    }

    private static class DateHelper implements Aspect {

        @Override
        public String before(final String application, final String body) {
            return null;
        }

        @Override
        public String after(final String application, final String body) {
            if (body.contains("my createDate")) {
                return "on createDate(y, m, d, h, min, s)\n" +
                        "\tset dateVar to (current date)\n" +
                        "\tset year of dateVar to y\n" +
                        "\tset month of dateVar to m\n" +
                        "\tset day of dateVar to d\n" +
                        "\tset hours of dateVar to h\n" +
                        "\tset minutes of dateVar to min\n" +
                        "\tset seconds of dateVar to s\n" +
                        "\treturn dateVar\n" +
                        "end createDate";
            } else {
                return null;
            }
        }
    }

}
