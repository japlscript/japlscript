/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.ReferenceImpl;
import com.tagtraum.japlscript.types.TypeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TestObjectInvocationHandler.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestObjectInvocationHandler {

    @Test(expected = JaplScriptException.class)
    public void testGetTypeClass() {
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(new ReferenceImpl("objRef", null));
        handler.getTypeClass();
    }

    @Test
    public void testIsSetReduceScriptExecutions() {
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(new ReferenceImpl("objRef", null));
        assertTrue(handler.isReduceScriptExecutions());
        handler.setReduceScriptExecutions(false);
        assertFalse(handler.isReduceScriptExecutions());
    }

    @Test
    public void testInvokeToString() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final Object s = handler.invoke(null, Object.class.getMethod("toString"), null);
        assertEquals("[application \"Finder\"]: null", s);
    }

    @Test
    public void testInvokeGetObjectReference() throws Throwable {
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(new ReferenceImpl("objRef", null));
        final Object s = handler.invoke(null, Reference.class.getMethod("getObjectReference"), null);
        assertEquals("objRef", s);
    }

    @Test
    public void testInvokeGetApplicationReference() throws Throwable {
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(new ReferenceImpl("objRef", "appRef"));
        final Object s = handler.invoke(null, Reference.class.getMethod("getApplicationReference"), null);
        assertEquals("appRef", s);
    }

    @Test
    public void testInvokeEquals() throws Throwable {
        final ReferenceImpl objRef0 = new ReferenceImpl("objRef0", null);
        final ReferenceImpl objRef0a = new ReferenceImpl("objRef0", null);
        final ReferenceImpl objRef1 = new ReferenceImpl("objRef1", null);
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(objRef0);
        final Boolean s = (Boolean)handler.invoke(null, Object.class.getMethod("equals", Object.class), new Object[]{objRef0});
        assertTrue(s);
        final Boolean sa = (Boolean)handler.invoke(null, Object.class.getMethod("equals", Object.class), new Object[]{objRef0a});
        assertTrue(sa);
        final Boolean s1 = (Boolean)handler.invoke(null, Object.class.getMethod("equals", Object.class), new Object[]{objRef1});
        assertFalse(s1);
    }

    @Test
    public void testInvokeHashcode() throws Throwable {
        final Reference objRef = new ReferenceImpl("objRef", null);
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(objRef);
        final Object s = handler.invoke(null, Object.class.getMethod("hashCode"), null);
        assertEquals(objRef.hashCode(), s);
    }

    @Test
    public void testInvokeCast() throws Throwable {
        final Reference objRef = new ReferenceImpl("objRef", null);
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(objRef);
        final Object s = handler.invoke(null, Reference.class.getMethod("cast", Class.class), new Object[]{String.class});
        assertEquals("objRef", s);
    }

    @Test
    public void testInvokeIsInstanceOf() throws Throwable {
        final Reference objRef = new ReferenceImpl("\"some String\"", null);
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(objRef);
        final Boolean s = (Boolean)handler.invoke(null, Reference.class.getMethod("isInstanceOf", TypeClass.class), new Object[]{Finder.CLASS});
        assertFalse(s);
        final Boolean s0 = (Boolean)handler.invoke(null, Reference.class.getMethod("isInstanceOf", TypeClass.class), new Object[]{TypeClass.getInstance("text", "\u00abclass text\u00bb", null, null)});
        assertTrue(s0);
    }

    @Test
    public void testGetProperty() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final String name = (String)handler.invoke(null, Finder.class.getMethod("getName", null), null);
        assertEquals("Finder", name);
    }

    @Test
    public void testCommand() throws Throwable {
        final Reference objRef = new ReferenceImpl("\"some String\"", null);
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final Boolean exists = (Boolean)handler.invoke(null, Finder.class.getMethod("exists",
            Reference.class), new Object[]{objRef});
        assertFalse(exists);
    }

    @Test(expected = JaplScriptException.class)
    public void testMake() throws Throwable {
        // we expect this to fail, because Finder cannot make a String.
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final Boolean exists = (Boolean)handler.invoke(null, Finder.class.getMethod("make",
            Class.class), new Object[]{String.class});
        assertFalse(exists);
    }

    /**
     * Stripped down Finder
     */
    @com.tagtraum.japlscript.Code("capp")
    @com.tagtraum.japlscript.Name("application")
    public interface Finder extends Reference {
        TypeClass CLASS = TypeClass.getInstance("application", "\u00abclass capp\u00bb", null, null);
        
        /**
         * Verify if an object exists.
         *
         * @param theObjectInQuestion the object in question
         * @return true if it exists, false if not
         */
        @com.tagtraum.japlscript.Kind("command")
        @com.tagtraum.japlscript.Name("exists")
        boolean exists(@Parameter("") com.tagtraum.japlscript.Reference theObjectInQuestion);

        /**
         * Creates a new object.
         * Make a new element.
         *
         * @param klass Java type of the object to create.
         * @return a new object of type klass
         */
        @com.tagtraum.japlscript.Kind("make")
        <T extends com.tagtraum.japlscript.Reference> T make(Class<T> klass);

        /**
         * The name of the application.
         */
        @com.tagtraum.japlscript.Type("text")
        @com.tagtraum.japlscript.Name("name")
        @com.tagtraum.japlscript.Code("pnam")
        @com.tagtraum.japlscript.Kind("property")
        java.lang.String getName();
    }

}
