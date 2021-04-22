/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.Boolean;
import com.tagtraum.japlscript.types.Double;
import com.tagtraum.japlscript.types.Float;
import com.tagtraum.japlscript.types.Integer;
import com.tagtraum.japlscript.types.Long;
import com.tagtraum.japlscript.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public final class JaplScript {

    private static final Logger LOG = LoggerFactory.getLogger(JaplScript.class);
    private static final int LAST_ASCII_CHAR = 127;
    private static final List<JaplType<?>> types = new ArrayList<>();
    private static final List<Aspect> globalAspects = new ArrayList<>();
    static {
        addDefaultGlobalAspects();
        addDefaultTypes();
    }

    private static void addDefaultTypes() {
        addType(Text.getInstance());
        addType(Integer.getInstance());
        addType(Long.getInstance());
        addType(Float.getInstance());
        addType(Double.getInstance());
        addType(Boolean.getInstance());
        addType(Date.getInstance());
        addType(Alias.getInstance());
        addType(Data.getInstance());
        addType(Picture.getInstance());
        addType(Tdta.getInstance());
        addType(JaplScriptFile.getInstance());
        addType(Point.getInstance());
        addType(Rectangle.getInstance());
        addType(RGBColor.getInstance());
        addType(TypeClass.getInstance());
        addType(Record.getInstance());
        addType(ReferenceImpl.getInstance());
    }

    private static void addDefaultGlobalAspects() {
        addGlobalAspect(new DateHelper());
        addGlobalAspect(new Tell());
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

    public static void addType(final JaplType<?> type) {
        types.add(type);
    }

    public static boolean removeType(final JaplType<?> type) {
        return types.remove(type);
    }

    /**
     * @return copy of the types list
     */
    public static List<JaplType<?>> getTypes() {
        return new ArrayList<>(types);
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
     * Casts a reference to a specific Java class.
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
            for (final JaplType<?> type : types) {
                if (interfaceClass == type._getInterfaceType()) {
                    return (T)type._parse(reference);
                }
            }
            if (interfaceClass.isArray()) {
                if (objectReference == null) {
                    return null;
                } else {
                    return (T) parseList(interfaceClass.getComponentType(), reference);
                }
            }
            if (objectReference != null && objectReference.trim().length() == 0) {
                return null;
            }
            if (JaplEnum.class.isAssignableFrom(interfaceClass) && JaplType.class.isAssignableFrom(interfaceClass)) {
                final T firstConstant = interfaceClass.getEnumConstants()[0];
                return ((JaplType<T>)firstConstant)._parse(reference);
            }
            if (!interfaceClass.isInterface()) {
                throw new JaplScriptException("Cannot create proxy for non-interface class " + interfaceClass);
            }
            return (T) Proxy.newProxyInstance(JaplScript.class.getClassLoader(),
                    new Class[]{interfaceClass}, new ObjectInvocationHandler(reference));
        } catch (JaplScriptException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new JaplScriptException("Failed to cast " + reference + " to " + interfaceClass);
        }
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
            final String hex = java.lang.Integer.toHexString(i);
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
