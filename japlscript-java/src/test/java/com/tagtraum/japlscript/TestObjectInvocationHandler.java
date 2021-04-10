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

    @Test
    public void testEmptyAspect() throws Throwable {
        final Session session = JaplScript.startSession();
        try {
            session.addAspect(new Aspect() {
                @Override
                public String before(final String application, final String body) {
                    return " ";
                }

                @Override
                public String after(final String application, final String body) {
                    return " ";
                }
            });
            session.addAspect(new Aspect() {
                @Override
                public String before(final String application, final String body) {
                    return null;
                }

                @Override
                public String after(final String application, final String body) {
                    return null;
                }
            });
            final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
            final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
            handler.invoke(null, Finder.class.getMethod("getItems", String.class), new Object[]{null});
        } finally {
            // ensure session is removed
            session.commit();
        }
    }

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
    public void testSetProperty() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        handler.setReduceScriptExecutions(false);
        final File file = (File)handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{File.class});
        final String origName = file.getName();
        file.setName("somename");
        // delete newly create file, a little hacky, because
        // file changes identity when you change its name
        final ReferenceImpl newFile = new ReferenceImpl(file.getObjectReference().replace(origName, "somename"), file.getApplicationReference());
        handler.invoke(null, Finder.class.getMethod("delete", Reference.class),
            new Object[]{newFile});
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

    @Test
    public void testGetElements() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final Item[] items = (Item[])handler.invoke(null, Finder.class.getMethod("getItems", String.class), new Object[]{null});
        assertNotNull(items);
        final int count = (int)handler.invoke(null, Finder.class.getMethod("countItems", String.class), new Object[]{null});
        assertEquals(count, items.length);
    }

    @Test
    public void testGetElementsWith() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final String whereClause = "name is \"saarblrbvlavnljBFLIukew\"";
        final Item[] items = (Item[])handler.invoke(null, Finder.class.getMethod("getItems", String.class), new Object[]{whereClause});
        // should not be possible to find
        assertArrayEquals(new Item[0], items);
        final int count = (int)handler.invoke(null, Finder.class.getMethod("countItems", String.class), new Object[]{whereClause});
        assertEquals(count, items.length);
    }

    @Test
    public void testGetElementWithIndex() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        handler.setReduceScriptExecutions(true);

        // ensure there is at least one file
        final File file = (File)handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{File.class});
        assertTrue(file.getObjectReference().startsWith("document file \""));
        try {
            final int count = (int) handler.invoke(null, Finder.class.getMethod("countItems", String.class), new Object[]{null});
            assertTrue(count > 0);
            final Item item = (Item) handler.invoke(null, Finder.class.getMethod("getItem", Integer.TYPE), new Object[]{0});
            assertNotNull(item);
        } finally {
            // delete newly create files
            handler.invoke(null, Finder.class.getMethod("delete", Reference.class), new Object[]{file});
        }
    }

    @Test
    public void testGetElementWithIndexUnreduced() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        handler.setReduceScriptExecutions(false);

        // ensure there is at least one file
        final File file = (File)handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{File.class});
        assertTrue(file.getObjectReference().startsWith("document file \""));
        try {
            final int count = (int) handler.invoke(null, Finder.class.getMethod("countItems", String.class), new Object[]{null});
            assertTrue(count > 0);
            final Item item = (Item) handler.invoke(null, Finder.class.getMethod("getItem", Integer.TYPE), new Object[]{0});
            assertNotNull(item);
        } finally {
            // delete newly create files
            handler.invoke(null, Finder.class.getMethod("delete", Reference.class), new Object[]{file});
        }
    }

    @Test
    public void testGetElementWithId() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        handler.setReduceScriptExecutions(true);
        final Item item = (Item)handler.invoke(null, Finder.class.getMethod("getItem", Id.class), new Object[]{new Id(0)});
        assertEquals("item id 0", item.getObjectReference());
    }

    @Test(expected = JaplScriptException.class)
    public void testGetElementWithIdUnreduced() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        handler.setReduceScriptExecutions(false);
        // we expect that there is no item with id = 0
        handler.invoke(null, Finder.class.getMethod("getItem", Id.class), new Object[]{new Id(0)});
    }

    @Test
    public void testMake() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final File file = (File)handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{File.class});
        assertTrue(file.getObjectReference().startsWith("document file \""));
        // delete newly create files
        handler.invoke(null, Finder.class.getMethod("delete", Reference.class), new Object[]{file});
    }

    @Test(expected = JaplScriptException.class)
    public void testMakeString() throws Throwable {
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
         * Move an item from its container to the trash.
         *
         * @param theItemToDelete the item to delete
         * @return to the item that was just deleted
         */
        @com.tagtraum.japlscript.Kind("command")
        @com.tagtraum.japlscript.Name("delete")
        com.tagtraum.japlscript.Reference delete(com.tagtraum.japlscript.Reference theItemToDelete);

        /**
         * The name of the application.
         */
        @com.tagtraum.japlscript.Type("text")
        @com.tagtraum.japlscript.Name("name")
        @com.tagtraum.japlscript.Code("pnam")
        @com.tagtraum.japlscript.Kind("property")
        java.lang.String getName();

        /**
         * @param value element to set in the list
         * @param index index into the element list
         */
        @com.tagtraum.japlscript.Type("item")
        @com.tagtraum.japlscript.Kind("element")
        void setItem(Item value, int index);

        /**
         * @return an array of all {@link Item}s
         */
        @com.tagtraum.japlscript.Type("item")
        @com.tagtraum.japlscript.Kind("element")
        default Item[] getItems() {
            return getItems(null);
        }

        /**
         * @param filter AppleScript filter clause without the leading "whose" or "where"
         * @return a filtered array of {@link Item}s
         */
        @com.tagtraum.japlscript.Type("item")
        @com.tagtraum.japlscript.Kind("element")
        Item[] getItems(String filter);

        /**
         * @param index index into the element list
         * @return the {@link Item} with at the requested index
         */
        @com.tagtraum.japlscript.Type("item")
        @com.tagtraum.japlscript.Kind("element")
        Item getItem(int index);

        /**
         * @param id id of the item
         * @return the {@link Item} with the requested id
         */
        @com.tagtraum.japlscript.Type("item")
        @com.tagtraum.japlscript.Kind("element")
        Item getItem(com.tagtraum.japlscript.Id id);

        /**
         * @return number of all {@link Item}s
         */
        @com.tagtraum.japlscript.Type("item")
        @com.tagtraum.japlscript.Kind("element")
        default int countItems() {
            return countItems(null);
        }

        /**
         * @param filter AppleScript filter clause without the leading "whose" or "where"
         * @return the number of elements that pass the filter
         */
        @com.tagtraum.japlscript.Type("item")
        @com.tagtraum.japlscript.Kind("element")
        int countItems(String filter);

        /**
         * Make a new element.
         *
         * @param theClassOfTheNewElement                                                                                    the class of the new element
         * @param theLocationAtWhichToInsertTheElement                                                                       the location at which to insert the element
         * @param whenCreatingAnAliasFileTheOriginalItemToCreateAnAliasToOrWhenCreatingAFileViewerWindowTheTargetOfTheWindow when creating an alias file, the original item to create an alias to or when creating a file viewer window, the target of the window
         * @param theInitialValuesForThePropertiesOfTheElement                                                               the initial values for the properties of the element
         * @return to the new object(s)
         */
        @com.tagtraum.japlscript.Kind("command")
        @com.tagtraum.japlscript.Name("make")
        com.tagtraum.japlscript.Reference make(@com.tagtraum.japlscript.Parameter("new") com.tagtraum.japlscript.Reference theClassOfTheNewElement, @com.tagtraum.japlscript.Parameter("at") com.tagtraum.japlscript.Reference theLocationAtWhichToInsertTheElement, @com.tagtraum.japlscript.Parameter("to") com.tagtraum.japlscript.Reference whenCreatingAnAliasFileTheOriginalItemToCreateAnAliasToOrWhenCreatingAFileViewerWindowTheTargetOfTheWindow, @com.tagtraum.japlscript.Parameter("with properties") com.tagtraum.japlscript.types.Record theInitialValuesForThePropertiesOfTheElement);

        /**
         * Creates a new object.
         * Make a new element.
         *
         * @param klass Java type of the object to create.
         * @return a new object of type klass
         */
        @com.tagtraum.japlscript.Kind("make")
        <T extends com.tagtraum.japlscript.Reference> T make(Class<T> klass);
    }

    /**
     * An item.
     */
    @com.tagtraum.japlscript.Plural("items")
    @com.tagtraum.japlscript.Code("cobj")
    @com.tagtraum.japlscript.Name("item")
    public interface Item extends com.tagtraum.japlscript.Reference {

        static final com.tagtraum.japlscript.types.TypeClass CLASS = com.tagtraum.japlscript.types.TypeClass.getInstance("item", "\u00abclass cobj\u00bb", null, null);

        /**
         * The name of the item.
         */
        @com.tagtraum.japlscript.Type("text")
        @com.tagtraum.japlscript.Name("name")
        @com.tagtraum.japlscript.Code("pnam")
        @com.tagtraum.japlscript.Kind("property")
        java.lang.String getName();

        /**
         * The name of the item.
         */
        @com.tagtraum.japlscript.Type("text")
        @com.tagtraum.japlscript.Name("name")
        @com.tagtraum.japlscript.Code("pnam")
        @com.tagtraum.japlscript.Kind("property")
        void setName(java.lang.String object);

        /**
         * The user-visible name of the item.
         */
        @com.tagtraum.japlscript.Type("text")
        @com.tagtraum.japlscript.Name("displayed name")
        @com.tagtraum.japlscript.Code("dnam")
        @com.tagtraum.japlscript.Kind("property")
        java.lang.String getDisplayedName();

    }

    /**
     * A file.
     */
    @com.tagtraum.japlscript.Plural("files")
    @com.tagtraum.japlscript.Code("file")
    @com.tagtraum.japlscript.Name("file")
    @com.tagtraum.japlscript.Inherits("item")
    public interface File extends com.tagtraum.japlscript.Reference, Item {

        static final com.tagtraum.japlscript.types.TypeClass CLASS = com.tagtraum.japlscript.types.TypeClass.getInstance("file", "\u00abclass file\u00bb", null, Item.CLASS);

        /**
         * The OSType identifying the type of data contained in the item.
         */
        @com.tagtraum.japlscript.Type("type")
        @com.tagtraum.japlscript.Name("file type")
        @com.tagtraum.japlscript.Code("asty")
        @com.tagtraum.japlscript.Kind("property")
        com.tagtraum.japlscript.Reference getFileType();

        /**
         * The OSType identifying the type of data contained in the item.
         */
        @com.tagtraum.japlscript.Type("type")
        @com.tagtraum.japlscript.Name("file type")
        @com.tagtraum.japlscript.Code("asty")
        @com.tagtraum.japlscript.Kind("property")
        void setFileType(com.tagtraum.japlscript.Reference object);

        /**
         * The OSType identifying the application that created the item.
         */
        @com.tagtraum.japlscript.Type("type")
        @com.tagtraum.japlscript.Name("creator type")
        @com.tagtraum.japlscript.Code("fcrt")
        @com.tagtraum.japlscript.Kind("property")
        com.tagtraum.japlscript.Reference getCreatorType();

        /**
         * The OSType identifying the application that created the item.
         */
        @com.tagtraum.japlscript.Type("type")
        @com.tagtraum.japlscript.Name("creator type")
        @com.tagtraum.japlscript.Code("fcrt")
        @com.tagtraum.japlscript.Kind("property")
        void setCreatorType(com.tagtraum.japlscript.Reference object);

        /**
         * Is the file a stationery pad?
         */
        @com.tagtraum.japlscript.Type("boolean")
        @com.tagtraum.japlscript.Name("stationery")
        @com.tagtraum.japlscript.Code("pspd")
        @com.tagtraum.japlscript.Kind("property")
        boolean getStationery();

        /**
         * Is the file a stationery pad?
         */
        @com.tagtraum.japlscript.Type("boolean")
        @com.tagtraum.japlscript.Name("stationery")
        @com.tagtraum.japlscript.Code("pspd")
        @com.tagtraum.japlscript.Kind("property")
        void setStationery(boolean object);

        /**
         * The version of the product (visible at the top of the “Get Info” window).
         */
        @com.tagtraum.japlscript.Type("text")
        @com.tagtraum.japlscript.Name("product version")
        @com.tagtraum.japlscript.Code("ver2")
        @com.tagtraum.japlscript.Kind("property")
        java.lang.String getProductVersion();

        /**
         * The version of the file (visible at the bottom of the “Get Info” window).
         */
        @com.tagtraum.japlscript.Type("text")
        @com.tagtraum.japlscript.Name("version")
        @com.tagtraum.japlscript.Code("vers")
        @com.tagtraum.japlscript.Kind("property")
        java.lang.String getVersion();

    }
}
