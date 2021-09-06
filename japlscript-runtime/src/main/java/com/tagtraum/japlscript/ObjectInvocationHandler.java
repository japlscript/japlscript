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
import com.tagtraum.japlscript.types.Record;
import com.tagtraum.japlscript.types.ReferenceImpl;
import com.tagtraum.japlscript.types.TypeClass;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.tagtraum.japlscript.JaplScript.cast;
import static com.tagtraum.japlscript.JaplScript.getProperty;

/**
 * Central invocation class, that maps dynamic proxy calls to generated AppleScript
 * snippets.
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
     * Creates the InvocationHandler.
     *
     * @param reference reference that methods are called upon
     */
    public ObjectInvocationHandler(final Reference reference) {
        this.reference = reference;
    }

    public boolean isReduceScriptExecutions() {
        return reduceScriptExecutions;
    }

    public void setReduceScriptExecutions(final boolean reduceScriptExecutions) {
        this.reduceScriptExecutions = reduceScriptExecutions;
    }

    public TypeClass getTypeClass() {
        try {
            return executeAppleScript(reference, "return class of " + reference.getObjectReference(), TypeClass.class);
        } catch (IOException e) {
            throw new JaplScriptException(e);
        }
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
                return args.length == 1 && args[0] != null && ((TypeClass) args[0]).isInstance(reference);
            } else if ("getProperties".equals(method.getName()) && (args == null || args.length == 0)) {
                return invokeProperties();
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
            return returnValue;
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw new JaplScriptException(e);
        }
    }

    private Map<String, Object> invokeProperties() throws IOException {
        final Record properties = executeAppleScript(reference, "return properties" + getOfClause(), Record.class);
        final Map<String, Reference> stringReferenceMap = (Map<String, Reference>)cast(Map.class, properties);
        final Map<String, Object> javaMap = new HashMap<>();
        for (final Map.Entry<String, Reference> e : stringReferenceMap.entrySet()) {
            final String propertyName = e.getKey();
            final Reference propertyValue = e.getValue();
            final Property property = getProperty(reference, propertyName);
            if (property != null) {
                javaMap.put(property.getJavaName(), cast(property.getJavaClass(), propertyValue));
            } else {
                LOG.warning("Failed to translate AppleScript property named \"" + propertyName + "\" to Java.");
                javaMap.put(propertyName, propertyValue);
            }
        }
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
                final String plural = method.getReturnType().getComponentType().getAnnotation(Plural.class).value();
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
                final String plural = method.getReturnType().getAnnotation(Plural.class).value();
                final int index = ((Integer) args[0] + 1);
                final String objectreference = "item " + index + " of " + plural + getOfClause();
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
                final String plural = method.getReturnType().getAnnotation(Plural.class).value();
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
            final String plural = getMethod.getReturnType().getComponentType().getAnnotation(Plural.class).value();
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

    private <T> T invokeProperty(final Method method, final Class<T> returnType, final Object[] args)
            throws IOException {
        T returnValue = null;
        final Name name = method.getAnnotation(Name.class);
        if (method.getName().startsWith("get")) {
            final String applescript = "return " + name.value() + getOfClause();
            returnValue = executeAppleScript(reference, applescript, returnType);
        } else if (method.getName().startsWith("set")) {
            final String applescript = "set " + name.value() + getOfClause() + " to " + encode(args[0]);
            returnValue = executeAppleScript(reference, applescript, returnType);
        }
        return returnValue;
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
            for (final JaplType<?> type : JaplScript.getTypes()) {
                if (type._getInterfaceType().isAssignableFrom(arg.getClass())) {
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
            if (!returnType.equals(Void.TYPE))
                return cast(returnType, new ReferenceImpl(returnValue, reference.getApplicationReference()));
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
                return cast(returnType, new ReferenceImpl(returnValue, reference.getApplicationReference()));
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

    private enum EncoderEnum implements JaplEnum, JaplType<EncoderEnum> {
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
        public EncoderEnum _parse(final String objectReference, final String applicationReference) {
            return null;
        }

        @Override
        public String _encode(final Object japlEnum) {
            return ((JaplEnum)japlEnum).getName();
        }

        @Override
        public Class<? extends EncoderEnum> _getInterfaceType() {
            return EncoderEnum.class;
        }


    }

}
