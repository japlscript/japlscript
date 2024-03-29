/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.execution.Aspect;
import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.japlscript.execution.Session;
import com.tagtraum.japlscript.language.ReferenceImpl;
import com.tagtraum.japlscript.language.TypeClass;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.logging.Logger;

/**
 * Central utility/runtime class.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public final class JaplScript {

    private static final Logger LOG = Logger.getLogger(JaplScript.class.getName());
    private static final int LAST_ASCII_CHAR = 127;
    private static final List<Codec<?>> types = new ArrayList<>();
    private static final List<Aspect> globalAspects = new ArrayList<>();
    private static final Map<String, Class<?>> applicationInterfaces = new HashMap<>();
    private static final Map<Class<?>, Map<TypeClass, Map<String, Property>>> applicationProperties = new HashMap<>();
    private static final Map<Class<?>, Map<TypeClass, Class<?>>> applicationClasses = new HashMap<>();
    private static final Map<String, Class<?>> APPLESCRIPT_TO_JAVA = new HashMap<>();
    private static final String SCRIPTING_ADDITION = "scripting addition";
    private static final String APPLICATION = "application";

    static {
        APPLESCRIPT_TO_JAVA.put("file specification", java.io.File.class);
        APPLESCRIPT_TO_JAVA.put("list", java.util.List.class);
        APPLESCRIPT_TO_JAVA.put("location reference", LocationReference.class);

        // TODO: Check these mappings and complete them.
        //       This https://gist.github.com/ccstone/955a0461d0ba02289b0cef469862ec84
        //       might come in handy.

        addDefaultGlobalAspects();
        addDefaultTypes();
    }


    private static void addDefaultTypes() {
        addType(com.tagtraum.japlscript.language.Text.getInstance());
        addType(com.tagtraum.japlscript.language.Integer.getInstance());
        addType(com.tagtraum.japlscript.language.Short.getInstance());
        addType(com.tagtraum.japlscript.language.Long.getInstance());
        addType(com.tagtraum.japlscript.language.Float.getInstance());
        addType(com.tagtraum.japlscript.language.Double.getInstance());
        addType(com.tagtraum.japlscript.language.Boolean.getInstance());
        addType(com.tagtraum.japlscript.language.Date.getInstance());
        addType(com.tagtraum.japlscript.language.Alias.getInstance());
        addType(com.tagtraum.japlscript.language.Data.getInstance());
        addType(com.tagtraum.japlscript.language.Picture.getInstance());
        addType(com.tagtraum.japlscript.language.Tdta.getInstance());
        addType(com.tagtraum.japlscript.language.JaplScriptFile.getInstance());
        addType(com.tagtraum.japlscript.language.Point.getInstance());
        addType(com.tagtraum.japlscript.language.Rectangle.getInstance());
        addType(com.tagtraum.japlscript.language.RGBColor.getInstance());
        addType(com.tagtraum.japlscript.language.TypeClass.getInstance());
        addType(com.tagtraum.japlscript.language.Record.getInstance());
        addType(com.tagtraum.japlscript.language.ReferenceImpl.getInstance());
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

    public static void addType(final Codec<?> type) {
        types.add(type);
        for (final TypeClass tc : type._getAppleScriptTypes()) {
            APPLESCRIPT_TO_JAVA.put(tc.getName().toLowerCase(), type._getJavaType());
        }
    }

    public static boolean removeType(final Codec<?> type) {
        return types.remove(type);
    }

    /**
     * @return copy of the types list
     */
    public static List<Codec<?>> getTypes() {
        return new ArrayList<>(types);
    }

    /**
     * Starts a session.
     *
     * @return thread specific session
     */
    public static Session startSession() {
        return Session.startSession();
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
        final Reference reference = new ReferenceImpl(null, APPLICATION +  " \"" + applicationName + "\"");
        registerApplicationInterface(interfaceClass, reference);
        registerApplicationProperties(interfaceClass);
        return cast(interfaceClass, reference);
    }

    /**
     * Gets the scripting addition object for a scripting addition.
     *
     * @param interfaceClass interface class
     * @param scriptingAdditionName scripting addition name, e.g. "StandardAdditions"
     * @param <T> t
     * @return scripting addition object
     */
    public static <T> T getScriptingAddition(final java.lang.Class<T> interfaceClass, final String scriptingAdditionName) {
        final Reference reference = new ReferenceImpl(null, SCRIPTING_ADDITION + " \"" + scriptingAdditionName + "\"");
        registerApplicationInterface(interfaceClass, reference);
        registerApplicationProperties(interfaceClass);
        return cast(interfaceClass, reference);
    }

    private static <T> void registerApplicationInterface(final Class<T> interfaceClass, final Reference reference) {
        // avoid registering twice, in order to avoid annoying messages.
        if (!applicationInterfaces.containsKey(reference.getApplicationReference())) {
            applicationInterfaces.put(reference.getApplicationReference(), interfaceClass);
        }
    }

    /**
     * Lookup a {@link TypeClass} instance declared in a <code>CLASS</code>
     * field of a generated class/interface.
     *
     * @param typeClass typeClass to use as lookup key
     * @return interned typeClass or, in case we didn't find a corresponding
     *  TypeClass instance the parameter
     */
    public static TypeClass internTypeClass(final TypeClass typeClass) {
        final Class<?> applicationInterface;
        if (typeClass.getApplicationInterface() != null) {
            applicationInterface = typeClass.getApplicationInterface();
        } else if (typeClass.getApplicationReference() != null) {
            applicationInterface = getApplicationInterface(typeClass);
            if (applicationInterface == null) {
                LOG.warning("TypeClass intern failure: Failed to find application " +
                    "interface for application reference " + typeClass.getApplicationReference());
                return typeClass;
            }
        } else {
            LOG.warning("TypeClass intern failure: Attempting to intern a TypeClass that has neither " +
                "an application interface nor an application reference: " + typeClass);
            return typeClass;
        }
        final Map<TypeClass, Map<String, Property>> typeClassMap = applicationProperties.get(applicationInterface);
        for (final TypeClass appTypeClass : typeClassMap.keySet()) {
            if (Objects.equals(appTypeClass.getCode(), typeClass.getCode()) || Objects.equals(appTypeClass.getName(), typeClass.getName())) {
                return appTypeClass;
            }
        }
        if (getStandardJavaType(typeClass.getObjectReference()) == null) {
            LOG.warning("TypeClass intern failure: TypeClass " + typeClass
                + " is not declared in " + applicationInterface.getName());
        }
        return typeClass;
    }

    private static Class<?> getApplicationInterface(final Reference reference) {
        return applicationInterfaces.get(reference.getApplicationReference());
    }

    public static Property getProperty(final Reference reference, final TypeClass typeClass, final String name) {
        if (reference.getApplicationReference() == null) {
            throw new JaplScriptException("Property lookup failure. Cannot lookup property for null application reference: " + reference);
        }
        final Class<?> applicationInterface = getApplicationInterface(reference);
        if (applicationInterface == null) {
            LOG.warning("An application interface for application reference " + reference.getApplicationReference() + " has not been registered.");
            return null;
        }
        final Map<TypeClass, Map<String, Property>> typeClassMap = applicationProperties.get(applicationInterface);
        for (TypeClass appTypeClass=typeClass; appTypeClass!=null; appTypeClass = appTypeClass.getSuperClass()) {
            if (!typeClassMap.containsKey(typeClass)) {
                LOG.warning("TypeClass " + typeClass  + " of property " + name
                    + " is not declared in " + applicationInterface.getSimpleName());
                return null;
            } else {
                final Property property = typeClassMap.get(typeClass).get(name);
                if (property != null) return property;
            }
        }
        return null;
    }

    private static void registerApplicationProperties(final Class<?> applicationInterface) {
        // avoid registering twice, in order to avoid annoying messages.
        if (!applicationProperties.containsKey(applicationInterface)) {
            final Map<TypeClass, Map<String, Property>> appMap = new HashMap<>();
            final Map<TypeClass, Class<?>> appClassMap = new HashMap<>();
            applicationProperties.put(applicationInterface, appMap);
            applicationClasses.put(applicationInterface, appClassMap);
            try {
                final Field applicationClassesField = applicationInterface.getField("APPLICATION_CLASSES");
                final Set<Class<?>> applicationClasses = (Set<Class<?>>) applicationClassesField.get(null);

                // first get all TypeClasses, so that we can intern them
                // while reading all Properties
                for (final Class<?> klass : applicationClasses) {
                    final TypeClass typeClass = TypeClass.fromClass(klass);
                    if (typeClass == null) {
                        throw new JaplScriptException("Generated class " + klass + " does not declare CLASS.");
                    }
                    appMap.put(typeClass, new HashMap<>());
                    appClassMap.put(typeClass, klass);
                }

                // now add all properties for each TypeClass, i.e. class declared in this app
                for (final Class<?> klass : applicationClasses) {
                    final Map<String, Property> classProperties = appMap.get(TypeClass.fromClass(klass));
                    final Set<Property> properties = Property.fromAnnotations(klass, applicationInterface);
                    for (final Property property : properties) {
                        classProperties.put(property.getName(), property);
                        classProperties.put(property.toChevron().toString(), property);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new JaplScriptException("Failure while registering application-wide properties", e);
            }
        }
    }

    /**
     * For a given reference, guess the most specific Java interface class, which is
     * identical to the given interface class or one of its subclasses.
     *
     * <p> This method is expected to <b>not</b> cause an AppleScript round trip, but instead
     * guess the correct type based on the given reference object. It takes advantage
     * of the fact that most references' object reference (or <a
     * href="https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/conceptual/ASLR_fundamentals.html#//apple_ref/doc/uid/TP40000983-CH218-SW7">Object
     * Specifier</a>)
     * starts with the object's class name, either as chevron encoded code or plain text name.
     * This is typically how object specifiers are returned from an AppleScript call.
     *
     * <p> So, if the object reference starts with a class name that is translated to a Java
     * class that happens to be identical to the given Java interface class or one of its
     * subclasses, we assume it's the correct class.
     *
     * <p> Note that this is not a perfect mapping, as object references can be
     * free-form AppleScripts (but usually aren't, especially when returned from
     * the scripting engine).
     *
     * @param interfaceClass Java interface class
     * @param reference AppleScript reference
     * @param <T> Java interface type param
     * @return either the given interface class or one of its subclasses
     */
    public static <T> java.lang.Class<? extends T> guessMostSpecificSubclass(final java.lang.Class<T> interfaceClass, final Reference reference) {
        if (reference == null) return interfaceClass;
        if (interfaceClass.isArray()) return interfaceClass;
        final Class<?> applicationInterface = getApplicationInterface(reference);
        if (applicationInterface == null) {
            LOG.fine("An application interface for application reference " + reference.getApplicationReference() + " has not been registered, so we cannot look up more specific types.");
            return interfaceClass;
        }
        final String objectReference = reference.getObjectReference();
        if (objectReference == null) {
            return interfaceClass;
        }

        final String trimmedObjectReference = objectReference.trim();
        TypeClass typeClass = null;
        if (trimmedObjectReference.startsWith("«class ")) {
            final String code = trimmedObjectReference.substring(0, trimmedObjectReference.indexOf("»") + 1);
            final TypeClass tc = new TypeClass(code, code, reference.getApplicationReference(), applicationInterface, null);
            typeClass = tc.intern();
        } else {
            final Set<TypeClass> typeClasses = applicationProperties.get(applicationInterface).keySet();
            for (final TypeClass tc : typeClasses) {
                if (trimmedObjectReference.startsWith(tc.getName())) {
                    typeClass = tc;
                    break;
                }
            }
        }
        if (typeClass != null) {
            final Class<?> interfaceClassSubClass = applicationClasses.get(applicationInterface).get(typeClass);
            if (!interfaceClass.equals(interfaceClassSubClass) && interfaceClassSubClass != null && interfaceClass.isAssignableFrom(interfaceClassSubClass)) {
                LOG.fine("Mapped requested class " + interfaceClass + " to more specific class " + interfaceClassSubClass + " for " + reference);
                return (Class<? extends T>)interfaceClassSubClass;
            }
        }
        return interfaceClass;
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
        return cast(interfaceClass, false, reference);
    }

    /**
     * Creates a suitable Java instance or dynamic proxy for the given {@link Reference}.
     *
     * @param interfaceClass interface class
     * @param useMostSpecificSubClass attempt to create an instance of the most specific subclass of interfaceClass
     *                                that is still suitable for the given reference
     * @param reference reference to create a Java instance for
     * @param <T> target type
     * @return object of type T
     */
    public static <T> T cast(final java.lang.Class<T> interfaceClass, final boolean useMostSpecificSubClass, final Reference reference) {
        if (reference == null) return null;
        try {
            final String objectReference = reference.getObjectReference();
            Class<? extends T> icc = interfaceClass;
            if (useMostSpecificSubClass) {
                icc = guessMostSpecificSubclass(interfaceClass, reference);
            }

            for (final Codec<?> type : types) {
                if (icc == type._getJavaType()) {
                    return (T)type._decode(reference);
                }
            }
            if (icc.isArray()) {
                if (objectReference == null) {
                    return null;
                } else {
                    return (T) parseList(icc.getComponentType(), useMostSpecificSubClass, reference);
                }
            }
            if (icc.equals(Map.class)) {
                if (objectReference == null) {
                    return (T) Collections.emptyMap();
                } else {
                    return (T) parseRecord(reference);
                }
            }
            if (objectReference != null && objectReference.trim().length() == 0) {
                return null;
            }
            if (JaplEnum.class.isAssignableFrom(icc) && Codec.class.isAssignableFrom(icc)) {
                final T firstConstant = icc.getEnumConstants()[0];
                return ((Codec<T>)firstConstant)._decode(reference);
            }
            if (!icc.isInterface()) {
                throw new JaplScriptException("Cannot create proxy for non-interface class " + icc);
            }

            return (T) Proxy.newProxyInstance(JaplScript.class.getClassLoader(),
                    new Class[]{icc}, new ObjectInvocationHandler(reference));
        } catch (JaplScriptException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new JaplScriptException("Failed to cast " + reference + " to " + interfaceClass, e);
        }
    }

    private static Object parseList(final Class<?> interfaceClass, final boolean useMostSpecificSubClass, final Reference reference) {
        final String objectReference = reference.getObjectReference();
        final String applicationReference = reference.getApplicationReference();
        //if (LOG.isLoggable(Level.FINE)) LOG.fine("objectReference: " + objectReference);
        //if (LOG.isLoggable(Level.FINE)) LOG.fine("applicationReference: " + applicationReference);
        //if (LOG.isLoggable(Level.FINE)) LOG.fine("interfaceClass: " + interfaceClass);
        final List<Object> result = new ArrayList<>();
        int depth = 0;
        boolean quotes = false;
        final boolean curlies = objectReference.startsWith("{");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objectReference.length(); i++) {
            final char c = objectReference.charAt(i);
            final char previousChar = i==0 ? 0 : objectReference.charAt(i-1);
            switch (c) {
                case '"':
                    if (previousChar != '\\') {
                        quotes = !quotes;
                    }
                    break;
                case '{':
                    depth++;
                    break;
                case '}':
                    depth--;
                    break;
                default:
            }
            final boolean lastChar = i == objectReference.length() - 1;
            if (quotes) {
                sb.append(c);
            } else if (depth == 1 && c == ',' || depth == 0 && c == '}' || !curlies && (c == ',' || lastChar)) {
                if (!curlies && lastChar) sb.append(c);
                if (sb.length() > 0) {
                    //if (LOG.isLoggable(Level.FINE)) LOG.fine("arr ref: " + sb);
                    result.add(cast(interfaceClass, useMostSpecificSubClass, new ReferenceImpl(sb.toString(), applicationReference)));
                    sb.setLength(0);
                }
            } else if (depth == 1 && c != '{') sb.append(c);
            else if (!curlies) sb.append(c);
            else if (depth > 1) sb.append(c);
        }
        final Object resultArray;
        if (interfaceClass.isPrimitive()) {
            resultArray = listToPrimitiveArray(interfaceClass, result);
        } else {
            resultArray = result.toArray((Object[]) Array.newInstance(interfaceClass, result.size()));
        }
        return resultArray;
    }

    /**
     * Convert a list of full blown objects to their corresponding primitive arrays.
     *
     * @param interfaceClass type of primitive
     * @param listOfObjects list of boxed primitives
     * @return array of primitives
     */
    private static Object listToPrimitiveArray(final Class<?> interfaceClass, final List<Object> listOfObjects) {
        Object resultArray = null;
        if (interfaceClass == java.lang.Integer.TYPE) {
            resultArray = listOfObjects.stream().mapToInt(i -> (java.lang.Integer) i).toArray();
        }
        else if (interfaceClass == java.lang.Long.TYPE) {
            resultArray = listOfObjects.stream().mapToLong(i -> (java.lang.Long) i).toArray();
        }
        else if (interfaceClass == java.lang.Double.TYPE) {
            resultArray = listOfObjects.stream().mapToDouble(i -> (java.lang.Double) i).toArray();
        }
        else if (interfaceClass == java.lang.Float.TYPE) {
            final float[] typedArray = new float[listOfObjects.size()];
            for (int i=0; i<typedArray.length; i++) {
                typedArray[i] = (java.lang.Float) listOfObjects.get(i);
            }
            resultArray = typedArray;
        }
        else if (interfaceClass == java.lang.Short.TYPE) {
            final short[] typedArray = new short[listOfObjects.size()];
            for (int i=0; i<typedArray.length; i++) {
                typedArray[i] = (java.lang.Short) listOfObjects.get(i);
            }
            resultArray = typedArray;
        }
        else if (interfaceClass == java.lang.Character.TYPE) {
            final char[] typedArray = new char[listOfObjects.size()];
            for (int i=0; i<typedArray.length; i++) {
                typedArray[i] = (java.lang.Character) listOfObjects.get(i);
            }
            resultArray = typedArray;
        }
        // I guess we don't really support bytes at this point
        /*
        else if (interfaceClass == Byte.TYPE) {
            final byte[] typedArray = new byte[listOfObjects.size()];
            for (int i=0; i<typedArray.length; i++) {
                typedArray[i] = (Byte) listOfObjects.get(i);
            }
            resultArray = typedArray;
        }
        */
        else if (interfaceClass == java.lang.Boolean.TYPE) {
            final boolean[] typedArray = new boolean[listOfObjects.size()];
            for (int i=0; i<typedArray.length; i++) {
                typedArray[i] = (java.lang.Boolean) listOfObjects.get(i);
            }
            resultArray = typedArray;
        }
        return resultArray;
    }

    private static java.util.Map<String, Reference> parseRecord(final Reference reference) {
        final String objectReference = reference.getObjectReference();
        final String applicationReference = reference.getApplicationReference();
        //if (LOG.isLoggable(Level.FINE)) LOG.fine("objectReference: " + objectReference);
        //if (LOG.isLoggable(Level.FINE)) LOG.fine("applicationReference: " + applicationReference);
        //if (LOG.isLoggable(Level.FINE)) LOG.fine("interfaceClass: " + interfaceClass);
        final Map<String, Reference> result = new HashMap<>();
        int depth = 0;
        boolean quotes = false;
        final boolean curlies = objectReference.startsWith("{");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objectReference.length(); i++) {
            final char c = objectReference.charAt(i);
            final char previousChar = i==0 ? 0 : objectReference.charAt(i-1);
            final boolean isLastChar = i == objectReference.length() - 1;
            switch (c) {
                case '"':
                    if (previousChar != '\\') {
                        quotes = !quotes;
                    }
                    break;
                case '{':
                    depth++;
                    break;
                case '}':
                    depth--;
                    break;
                default:
            }
            if (quotes) {
                sb.append(c);
            } else if (depth == 1 && c == ',' || depth == 0 && c == '}' || !curlies && (c == ',' || isLastChar)) {
                if (!curlies && isLastChar) sb.append(c);
                if (sb.length() > 0) {
                    // split into key and value
                    final int afterKey = sb.indexOf(":");
                    final String key = sb.substring(0, afterKey).trim();
                    final String value = sb.substring(afterKey + 1).trim();
                    result.put(key, ReferenceImpl.getInstance()._decode(value, applicationReference));
                    sb.setLength(0);
                }
            } else if (depth == 1 && c != '{') sb.append(c);
            else if (!curlies) sb.append(c);
            else if (depth > 1) sb.append(c);
        }
        return result;
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
        for (int i = 0, max=s.length(); i < max; i++) {
            final char c = s.charAt(i);
            if (c == '"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        sb.append("\")");
        return sb.toString();
    }

    /**
     *
     * @param applescriptType AppleScript type
     * @return the standard Java type or null, if none is defined
     */
    public static String getStandardJavaType(final String applescriptType) {
        if (applescriptType == null) {
            return null;
        }
        if (applescriptType.equalsIgnoreCase("record")) {
            return Map.class.getName() + "<" + String.class.getName() + ", " + Reference.class.getName() + ">";
        }
        final String lowercaseAppleScriptType = applescriptType.toLowerCase();
        final Class<?> javaType = APPLESCRIPT_TO_JAVA.get(lowercaseAppleScriptType);
        if (javaType != null) return javaType.getName();
        return null;
    }

    private static class Tell implements Aspect {

        @Override
        public String before(final String application, final String body) {
            // only surround true applications with "tells"
            if (application != null && !application.startsWith(SCRIPTING_ADDITION))
                return "tell " + application;
            else
                return "";
        }

        @Override
        public String after(final String application, final String body) {
            // only surround true applications with "tells"
            if (application != null && !application.startsWith(SCRIPTING_ADDITION))
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
