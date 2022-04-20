/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.execution.Aspect;
import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.japlscript.execution.ScriptExecutor;
import com.tagtraum.japlscript.execution.Session;
import com.tagtraum.japlscript.language.Record;
import com.tagtraum.japlscript.language.ReferenceImpl;
import com.tagtraum.japlscript.language.TypeClass;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.tagtraum.japlscript.JaplScript.*;

/**
 * Central invocation class (for a reference), that maps
 * dynamic proxy calls to generated AppleScript snippets
 * and executes them via the {@link ScriptExecutor}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ObjectInvocationHandler implements InvocationHandler {

    private static final Logger LOG = Logger.getLogger(ObjectInvocationHandler.class.getName());
    private static final Method TO_STRING_METHOD;
    private static final Method EQUALS_METHOD;
    private static final Method HASHCODE_METHOD;
    private static final Method OBJECT_REFERENCE_METHOD;
    private static final Method APPLICATION_REFERENCE_METHOD;
    private static final Method CAST_METHOD;
    private static final Method IS_INSTANCE_OF_METHOD;

    static {
        try {
            TO_STRING_METHOD = Object.class.getMethod("toString");
            HASHCODE_METHOD = Object.class.getMethod("hashCode");
            EQUALS_METHOD = Object.class.getMethod("equals", Object.class);
            OBJECT_REFERENCE_METHOD = Reference.class.getMethod("getObjectReference");
            APPLICATION_REFERENCE_METHOD = Reference.class.getMethod("getApplicationReference");
            CAST_METHOD = Reference.class.getMethod("cast", Class.class);
            IS_INSTANCE_OF_METHOD = Reference.class.getMethod("isInstanceOf", TypeClass.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    private final Reference reference;
    private boolean reduceScriptExecutions = true;

    /**
     * Creates the {@link InvocationHandler} for a given {@link Reference}.
     *
     * @param reference reference that methods are called upon
     */
    public ObjectInvocationHandler(final Reference reference) {
        this.reference = reference;
    }

    /**
     * In some situations, Japlscript may simply construct a new
     * <a href="https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/conceptual/ASLR_fundamentals.html#//apple_ref/doc/uid/TP40000983-CH218-SW7">object specifier</a>
     * rather than asking the runtime to return the object described by the
     * object specifier. Since asking the system is another roundtrip,
     * this is avoided when this property is true.
     * <p>
     * For example, when asking for an element by index, one could
     * simply return a new (Java) {@link Reference} that points to
     * <code>item 5 of CoolElements of "FantasticApp"</code> (reduction <em>on</em>)
     * or actually execute the code<br>
     * <code>tell application "FantasticApp"<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;return item 5 of CoolElements of "FantasticApp"<br>
     * end tell<br>
     * </code>
     * and then return a reference to the result (reduction <em>off</em>).
     * <p>
     * By default, reduction is <em>on</em>.
     * <p>
     * Note that the result may be different, if the actual AppleScript call does
     * not return an object specifier, but the actual object.
     *
     * @return true or false
     */
    public boolean isReduceScriptExecutions() {
        return reduceScriptExecutions;
    }

    /**
     * Attempt to save some roundtrips.
     *
     * @param reduceScriptExecutions true or false
     * @see #isReduceScriptExecutions()
     */
    public void setReduceScriptExecutions(final boolean reduceScriptExecutions) {
        this.reduceScriptExecutions = reduceScriptExecutions;
    }

    /**
     * Execute AppleScript to return the (AppleScript) class of the current reference.
     *
     * @return Java representation of the AppleScript class
     */
    public TypeClass getTypeClass() {
        try {
            final TypeClass typeClass = executeAppleScript(reference, "return class of " + reference.getObjectReference(), TypeClass.class);
            return typeClass.intern();
        } catch (IOException e) {
            throw new JaplScriptException(e);
        }
    }

    /**
     * Get {@link TypeClass} based on the current reference and the given property map.
     *
     * @param propertyMap map from property name/chevron to reference
     * @param interfaceClass Java interface class the property map belongs to, i.e.,
 *                           that {@code getProperties()} was invoked on
     * @return type class
     * @see #invokeProperties(Class)
     */
    private TypeClass getTypeClass(final Map<String, Reference> propertyMap, final Class<?> interfaceClass) {
        // Is it always just "class"/<<property pcls>> ?
        // Or are there other possible values?
        TypeClass typeClass;
        Reference classRef = propertyMap.get(new Chevron("property", "pcls").toString());
        if (classRef != null) {
            typeClass = new TypeClass(null, classRef.getObjectReference(), reference.getApplicationReference(), null).intern();
        } else {
            classRef = propertyMap.get("class");
            if (classRef != null) {
                typeClass = new TypeClass(classRef.getObjectReference(), null, reference.getApplicationReference(), null).intern();
            } else {
                typeClass = getTypeClass();
                if (typeClass.getCode().getCode().equals("reco")) {
                    try {
                        return (TypeClass)interfaceClass.getDeclaredField("CLASS").get(null);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new JaplScriptException("Failed to lookup TypeClass for " + interfaceClass.getName(), e);
                    }
                }
            }
        }
        return typeClass;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        try {
            Object returnValue = null;
            // check standard/JaplScript methods
            if (TO_STRING_METHOD.equals(method)) {
                return toString(reference);
            } else if (OBJECT_REFERENCE_METHOD.equals(method)) {
                return reference.getObjectReference();
            } else if (EQUALS_METHOD.equals(method)) {
                return equals(reference, args[0]);
            } else if (HASHCODE_METHOD.equals(method)) {
                return reference.hashCode();
            } else if (APPLICATION_REFERENCE_METHOD.equals(method)) {
                return reference.getApplicationReference();
            } else if (CAST_METHOD.equals(method)) {
                return cast((Class<?>) args[0], reference);
            } else if (IS_INSTANCE_OF_METHOD.equals(method)) {
                if (args.length != 1 || args[0] == null) return false;
                final TypeClass typeClass = ((TypeClass) args[0]).intern();
                return typeClass.isInstance(reference);
            } else if ("getProperties".equals(method.getName()) && (args == null || args.length == 0)) {
                return invokeProperties(method.getDeclaringClass());
            }
            final Kind kind = method.getAnnotation(Kind.class);
            // interface methods
            if ("element".equals(kind.value())) {
                returnValue = invokeElement(method, method.getReturnType(), args);
            } else if ("property".equals(kind.value())) {
                returnValue = invokeProperty(method, method.getReturnType(), args);
            } else if ("command".equals(kind.value())) {
                returnValue = invokeCommand(method, method.getReturnType(), args);
            } else if ("make".equals(kind.value())) {
                returnValue = invokeMake(method, args);
            }
            if (returnValue instanceof TypeClass && ((TypeClass) returnValue).getApplicationReference() != null) {
                returnValue = JaplScript.internTypeClass((TypeClass) returnValue);
            }
            return returnValue;
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw new JaplScriptException(e);
        }
    }

    private Map<String, Object> invokeProperties(final Class<?> interfaceClass) throws IOException {
        final Reference properties;
        if (!isRecord()) {
            properties = executeAppleScript(reference, "return properties" + getOfClause(), Record.class);
        } else {
            properties = reference;
        }
        return toJavaMap(properties, interfaceClass);
    }

    private Map<String, Object> toJavaMap(final Reference record, final Class<?> interfaceClass) {
        final Map<String, Reference> stringReferenceMap = (Map<String, Reference>)cast(Map.class, record);
        final TypeClass typeClass = getTypeClass(stringReferenceMap, interfaceClass);

        final Map<String, Object> javaMap = new HashMap<>();
        for (final Map.Entry<String, Reference> e : stringReferenceMap.entrySet()) {
            final String propertyName = e.getKey();
            final Reference propertyValue = e.getValue();
            final Property property = getProperty(this.reference, typeClass, propertyName);
            if (property != null) {
                javaMap.put(property.getJavaName(), cast(property.getJavaClass(), true, propertyValue));
            } else {
                LOG.warning("Failed to translate AppleScript property named \"" + propertyName + "\" to Java.");
                javaMap.put(propertyName, propertyValue);
            }
        }
        // TODO: add type class or something, if not present?
        return javaMap;
    }

    private String toString(final Reference reference) {
        return "[" + reference.getApplicationReference() + "]: " + reference.getObjectReference();
    }

    private boolean equals(final Reference ref1, final Object ref2) {
        if (ref2 == null) return false;
        if (!(ref2 instanceof Reference)) return false;
        return toString(ref1).equals(toString((Reference)ref2));
    }

    private <T> T invokeMake(final Method method, final Object... args) throws IOException {
        if (args.length != 1) {
            throw new JaplScriptException("Wrong number of arguments for " + method + ": " + args.length);
        }
        if (!(args[0] instanceof Class)) {
            throw new JaplScriptException("Argument is not a class object: " + args[0].getClass());
        }
        final Class<T> klass = (Class<T>) args[0];
        final Name applescriptClassname = klass.getAnnotation(Name.class);
        if (applescriptClassname == null) {
            throw new IOException("\"make\" failed, because we failed to find a Name annotation for class " + klass);
        }
        final String applescript = "make " + applescriptClassname.value();
        return executeAppleScript(reference, applescript, klass);
    }

    private <T> T invokeCommand(final Method method, final Class<T> returnType,
                                final Object... args) throws IOException {
        final Name name = method.getAnnotation(Name.class);
        final Parameter[] parameters = getFirstParameterAnnotations(method);
        final StringBuilder applescript = new StringBuilder(name.value() + " ");
        //if (LOG.isLoggable(Level.FINE)) LOG.fine(Arrays.asList(parameters.value()));
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                final Object arg = args[i];
                if (arg == null) continue;
                if (parameters[i] != null) {
                    applescript.append(parameters[i].value());
                }
                applescript.append(' ');
                applescript.append(encode(arg));
                applescript.append(" ");
            }
        }
        return executeAppleScript(reference, applescript.toString(), returnType);
    }

    private static Parameter[] getFirstParameterAnnotations(final Method method) {
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final Parameter[] parameters = new Parameter[parameterAnnotations.length];
        int j=0;
        for (final Annotation[] anns : parameterAnnotations) {
            if (anns.length > 0) parameters[j] = (Parameter) anns[0];
            j++;
        }
        return parameters;
    }

    private <T> T invokeElement(final Method method, final Class<T> returnType, final Object... args)
            throws IOException, NoSuchMethodException {
        T returnValue = null;
        final Type type = method.getAnnotation(Type.class);
        if (method.getName().startsWith("get")) {
            if (method.getReturnType().isArray()) {
                final String plural = getPlural(method.getReturnType().getComponentType());
                final String applescript;
                if (args != null && args[0] != null && !((String)args[0]).trim().isEmpty() && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == String.class) {
                    applescript = "return " + plural + getOfClause() + " where " + args[0];
                } else if (args == null || args[0] == null || ((String)args[0]).trim().isEmpty() || method.getParameterTypes().length == 0) {
                    applescript = "return " + plural + getOfClause();
                } else {
                    throw new JaplScriptException("Unknown method signature. " + method);
                }
                returnValue = executeAppleScript(reference, applescript, returnType);
            } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == Integer.TYPE) {
                final int index = ((Integer) args[0] + 1);
                final String objectreference = type.value() + " " + index + getOfClause();
                if (reduceScriptExecutions) {
                    if (index < 1) throw new ArrayIndexOutOfBoundsException("Index has to be greater than 0");
                    returnValue = cast(returnType,
                            new ReferenceImpl(objectreference, reference.getApplicationReference()));
                } else {
                    final String applescript = "return " + objectreference;
                    returnValue = executeAppleScript(reference, applescript, returnType);
                }
            } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == Id.class) {
                final Id id = (Id) args[0];
                final String objectreference = type.value() + " " + id + getOfClause();
                if (reduceScriptExecutions) {
                    returnValue = cast(returnType,
                            new ReferenceImpl(objectreference, reference.getApplicationReference()));
                } else {
                    final String applescript = "return " + objectreference;
                    returnValue = executeAppleScript(reference, applescript, returnType);
                }
            } else {
                throw new JaplScriptException("Unknown method signature. " + method);
            }
        } else if (method.getName().startsWith("set")) {
            // this is untested and probably does not work
            // generation of element setters is disabled by default
            if (method.getParameterTypes().length == 2 && method.getParameterTypes()[0] == Integer.TYPE) {
                final String plural = getPlural(method.getReturnType());
                final int index = ((Integer) args[0] + 1);
                final Reference ref = (Reference) args[1];
                // really?
                final String applescript = "set item " + index + " of " + plural + getOfClause() + " to (" + ref.getObjectReference() + ")";
                executeAppleScript(reference, applescript, returnType);
            } else {
                throw new JaplScriptException("Unknown method signature. " + method);
            }
        } else if (method.getName().startsWith("count")) {
            final Method getMethod = method.getDeclaringClass().getMethod("get"
                    + method.getName().substring("count".length()));
            final String plural = getPlural(getMethod.getReturnType().getComponentType());
            final String applescript;
            if (args != null && args[0] != null && !((String) args[0]).trim().isEmpty() && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == String.class) {
                applescript = "count " + plural + getOfClause() + " where " + args[0];
            } else if (args == null || args[0] == null || ((String)args[0]).trim().isEmpty() || method.getParameterTypes().length == 0) {
                applescript = "count " + plural + getOfClause();
            } else {
                throw new JaplScriptException("Unknown method signature. " + method);
            }
            returnValue = executeAppleScript(reference, applescript, returnType);
        }
        return returnValue;
    }

    /**
     * Find the AppleScript plural for a type.
     *
     * @param type type
     * @return the plural or its singular, if no plural is defined (e.g. Photos.app).
     */
    private String getPlural(final Class<?> type) {
        final Plural pluralAnnotation = type.getAnnotation(Plural.class);
        final String plural;
        if (pluralAnnotation != null) {
            plural = pluralAnnotation.value();
        }
        else {
            LOG.warning("Type " + type.getName() + " does not have a defined AppleScript plural. " +
                "Trying to simply add an 's'.");
            plural = type.getAnnotation(Name.class).value() + "s";
        }
        return plural;
    }

    private <T> T invokeProperty(final Method method, final Class<T> returnType, final Object[] args)
            throws IOException {
        T returnValue = null;
        final Name name = method.getAnnotation(Name.class);
        final Code code = method.getAnnotation(Code.class);
        if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
            final String applescript = "return " + name.value() + getOfClause();
            returnValue = executeAppleScript(reference, applescript, returnType);

//            if (!isRecord()) {
//                final String applescript = "return " + name.value() + getOfClause();
//                returnValue = executeAppleScript(reference, applescript, returnType);
//            } else {
//                // let's assume we have record
//                final Map<String, Reference> stringReferenceMap = (Map<String, Reference>)cast(Map.class, reference);
//                if (stringReferenceMap.containsKey(code)) {
//                    returnValue = cast(returnType, stringReferenceMap.get(code));
//                } else if (stringReferenceMap.containsKey(name)) {
//                    returnValue = cast(returnType, stringReferenceMap.get(name));
//                } else {
//                    returnValue = null;
//                }
//            }
        } else if (method.getName().startsWith("set")) {
            final String applescript = "set " + name.value() + getOfClause() + " to " + encode(args[0]);
            returnValue = executeAppleScript(reference, applescript, returnType);
        }
        return returnValue;
    }

    private boolean isRecord() {
        return reference != null
            && reference.getObjectReference() != null
            && reference.getObjectReference().startsWith("{")
            && reference.getObjectReference().endsWith("}");
    }

    private String getOfClause() {
        if (reference.getObjectReference() == null) return "";
        else return " of " + reference.getObjectReference();
    }

    private String encode(final Object arg) {
        if (arg instanceof Object[]) return encode((Object[]) arg);
        else if (arg instanceof java.util.List) return encode((List<?>) arg);
        else if (arg instanceof java.util.Map) return encode((Map<String, ?>) arg);
        else {
            // all regular types from JaplScript
            for (final Codec<?> type : JaplScript.getTypes()) {
                if (type._getJavaType().isAssignableFrom(arg.getClass())) {
                    return type._encode(arg);
                }
            }
            // special case: enums
            if (JaplEnum.class.isAssignableFrom(arg.getClass())) {
                return EncoderEnum.DUMMY._encode(arg);
            }
        }
        return arg.toString();
    }

    private String encode(final Object[] array) {
        return Arrays.stream(array)
            .map(this::encode)
            .collect(Collectors.joining(", ", "{", "}"));
    }

    private String encode(final List<?> list) {
        return list.stream()
            .map(this::encode)
            .collect(Collectors.joining(", ", "{", "}"));
    }

    private String encode(final Map<String, ?> map) {
        return map.entrySet().stream()
            .map(e -> e.getKey() + ": " + encode(e.getValue()))
            .collect(Collectors.joining(", ", "{", "}"));
    }

    public <T> T executeAppleScript(final Reference reference, final String appleScript, final Class<T> returnType) throws IOException {
        return executeAppleScript(reference.getApplicationReference(), appleScript, returnType);
    }

    private <T> T executeAppleScript(final String application, final String appleScript, final Class<T> returnType) throws IOException {
        return executeAppleScript(tell(application, appleScript), returnType);
    }

    private <T> T executeAppleScript(final CharSequence appleScript, final Class<T> returnType) throws IOException {
        final Session session = Session.getSession();
        if (session == null) {
            final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
            scriptExecutor.setScript(appleScript);
            final String returnValue = scriptExecutor.execute();
            if (LOG.isLoggable(Level.FINE)) LOG.fine(appleScript + " == > " + returnValue);
            final ReferenceImpl reference = new ReferenceImpl(returnValue, this.reference.getApplicationReference());
            if (!returnType.equals(Void.TYPE)) {
                return cast(guessMostSpecificSubclass(returnType, reference), true, reference);
            }
            return null;
        } else if (returnType.equals(Void.TYPE) || session.isIgnoreReturnValues()) {
            session.add(appleScript);
        } else {
            try {
                // implicit commit forced by a return value
                session.add(appleScript);
                final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
                scriptExecutor.setScript(session.getScript());
                final String returnValue = scriptExecutor.execute();
                return cast(guessMostSpecificSubclass(returnType, reference), new ReferenceImpl(returnValue, reference.getApplicationReference()));
            } finally {
                session.reset();
            }
        }
        return null;
    }

    private CharSequence tell(final String application, final String appleScript) {
        final StringBuilder sb = new StringBuilder();
        final List<Aspect> globalAspects = JaplScript.getGlobalAspects();
        for (final Aspect aspect : globalAspects) {
            final String before = aspect.before(application, appleScript);
            if (before != null) sb.append(before).append("\r\n");
        }


        final Session session = Session.getSession();
        if (session != null) {
            final List<Aspect> aspects = session.getAspects();
            for (final Aspect aspect : aspects) {
                final String before = aspect.before(application, appleScript);
                if (before != null) sb.append("  ").append(before).append("\r\n");
            }
            sb.append("  ").append(appleScript).append("\r\n");
            Collections.reverse(aspects);
            for (final Aspect aspect : aspects) {
                final String after = aspect.after(application, appleScript);
                if (after != null) sb.append("  ").append(after).append("\r\n");
            }
        }
        else {
            sb.append(appleScript).append("\r\n");
        }

        Collections.reverse(globalAspects);
        for (final Aspect aspect : globalAspects) {
            final String after = aspect.after(application, appleScript);
            if (after != null) sb.append(after).append("\r\n");
        }
        return sb.toString();
    }

    private enum EncoderEnum implements JaplEnum, Codec<EncoderEnum> {
        DUMMY;


        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getCode() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public EncoderEnum _decode(final String objectReference, final String applicationReference) {
            return null;
        }

        @Override
        public String _encode(final Object japlEnum) {
            return ((JaplEnum)japlEnum).getName();
        }

        @Override
        public Class<? extends EncoderEnum> _getJavaType() {
            return EncoderEnum.class;
        }

        @Override
        public TypeClass[] _getAppleScriptTypes() {
            return new TypeClass[0];
        }


    }

}
