/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.execution.Aspect;
import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.japlscript.execution.Session;
import com.tagtraum.japlscript.types.ReferenceImpl;
import com.tagtraum.japlscript.types.TypeClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testGetTypeClass() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            final ObjectInvocationHandler handler = new ObjectInvocationHandler(new ReferenceImpl("objRef", null));
            handler.getTypeClass();
        });
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
        final Boolean s0 = (Boolean)handler.invoke(null, Reference.class.getMethod("isInstanceOf", TypeClass.class), new Object[]{TypeClass.getInstance("text", "\u00abclass ctxt\u00bb", null, null)});
        assertTrue(s0);
    }

    @Test
    public void testGetProperties() throws Throwable {
        // registers properties etc.
        JaplScript.getApplication(Finder.class, "Finder");

        // hack for Github - somehow Finder does not have any properties there
        // so instead we use the home folder.
        final Reference finder = new ReferenceImpl("(path to home folder)", "application \"Finder\"");

        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final Map<String, Object> properties = (Map<String, Object>)handler.invoke(null, File.class.getMethod("getProperties", null), null);
        // NOTE: correct result is limited to what we declare in the Finder test class
        //       this means that we will see warnings for all undeclared properties.
        assertEquals(System.getProperty("user.name"), properties.get("name"));
        assertTrue(properties.containsKey("klass"), "We are missing the property \"klass\". Properties we have found: " + properties);
        final TypeClass klass = (TypeClass) properties.get("klass");
        assertEquals("«class cfol»", klass.getCode());
        // TODO: test other props? if so, we must add them to the test Finder class below
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
    public void testSetEnumerationProperty() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        handler.setReduceScriptExecutions(false);
        final File file = (File)handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{File.class});
        file.setOwnerPrivileges(Priv.READ_WRITE);
        // delete newly create file, a little hacky, because
        // file changes identity when you change its name
        handler.invoke(null, Finder.class.getMethod("delete", Reference.class), new Object[]{file});
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
        final Finder finder = Finder.getInstance();
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
        assertTrue(file.getObjectReference().startsWith("document file \"") || file.getObjectReference().startsWith("«class docf» \""), "got: " + file.getObjectReference());
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
    public void testGetElementWithIndexUnreduced() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
            final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
            handler.setReduceScriptExecutions(false);

            // ensure there is at least one file
            final File file = (File) handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{File.class});
            assertTrue(file.getObjectReference().startsWith("document file \"") || file.getObjectReference().startsWith("«class docf» \""));
            try {
                final int count = (int) handler.invoke(null, Finder.class.getMethod("countItems", String.class), new Object[]{null});
                assertTrue(count > 0);
                // this must fail, as AppleScript is trying to load the file's data. Results in a -1728 error
                handler.invoke(null, Finder.class.getMethod("getItem", Integer.TYPE), new Object[]{0});
            } finally {
                // delete newly create files
                handler.invoke(null, Finder.class.getMethod("delete", Reference.class), new Object[]{file});
            }
        });
    }

    @Test
    public void testGetElementWithId() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        handler.setReduceScriptExecutions(true);
        final Item item = (Item)handler.invoke(null, Finder.class.getMethod("getItem", Id.class), new Object[]{new Id(0)});
        assertEquals("item id 0", item.getObjectReference());
    }

    @Test
    public void testGetElementWithIdUnreduced() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
            final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
            handler.setReduceScriptExecutions(false);
            // we expect that there is no item with id = 0
            handler.invoke(null, Finder.class.getMethod("getItem", Id.class), new Object[]{new Id(0)});
        });
    }

    @Test
    public void testMake() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final File file = (File)handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{File.class});
        try {
            assertTrue(file.getObjectReference().startsWith("document file \"")
                || file.getObjectReference().startsWith("«class docf» \""));
            // delete newly create files
        } finally {
            handler.invoke(null, Finder.class.getMethod("delete", Reference.class), new Object[]{file});
        }
    }

    @Test
    public void testMakeWithWrongArgumentType() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
            final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
            handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{""});
        });
    }

    @Test
    public void testMakeWithWrongArgumentCount() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
            final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
            handler.invoke(null, Finder.class.getMethod("make", Class.class), new Object[]{"", ""});
        });
    }

    @Test
    public void testMakeString() {
        Assertions.assertThrows(JaplScriptException.class, () -> {
            // we expect this to fail, because Finder cannot make a String.
            final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
            final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
            final Boolean exists = (Boolean) handler.invoke(null, Finder.class.getMethod("make",
                Class.class), new Object[]{String.class});
            assertFalse(exists);
        });
    }

    /**
     * Stripped down Finder
     */
    @com.tagtraum.japlscript.Code("capp")
    @com.tagtraum.japlscript.Name("application")
    public interface Finder extends Reference {
        TypeClass CLASS = TypeClass.getInstance("application", "\u00abclass capp\u00bb", null, null);
        Set<java.lang.Class<?>> APPLICATION_CLASSES = new java.util.HashSet<>(java.util.Arrays.asList(Finder.class, Item.class, File.class));

        static Finder getInstance() {
            return JaplScript.getApplication(Finder.class, "Finder");
        }

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

        /**
         * Returns all properties for an instance of this class.
         *
         * @return Map containing all properties
         */
        Map<String, Object> getProperties();
    }

    /**
     * An item.
     */
    @com.tagtraum.japlscript.Plural("items")
    @com.tagtraum.japlscript.Code("cobj")
    @com.tagtraum.japlscript.Name("item")
    public interface Item extends com.tagtraum.japlscript.Reference {

        com.tagtraum.japlscript.types.TypeClass CLASS = com.tagtraum.japlscript.types.TypeClass.getInstance("item", "\u00abclass cobj\u00bb", null, null);

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

        /**
         *
         */
        @com.tagtraum.japlscript.Type("priv")
        @com.tagtraum.japlscript.Name("owner privileges")
        @com.tagtraum.japlscript.Code("ownr")
        @com.tagtraum.japlscript.Kind("property")
        Priv getOwnerPrivileges();

        /**
         *
         */
        @com.tagtraum.japlscript.Type("priv")
        @com.tagtraum.japlscript.Name("owner privileges")
        @com.tagtraum.japlscript.Code("ownr")
        @com.tagtraum.japlscript.Kind("property")
        void setOwnerPrivileges(Priv object);

        /**
         * The class of the item.
         *
         * @return Property value
         */
        @com.tagtraum.japlscript.Code("pcls")
        @com.tagtraum.japlscript.Kind("property")
        @com.tagtraum.japlscript.Name("class")
        @com.tagtraum.japlscript.Type("type")
        com.tagtraum.japlscript.types.TypeClass getKlass();

        /**
         * Returns all properties for an instance of this class.
         *
         * @return Map containing all properties
         */
        Map<String, Object> getProperties();
    }

    /**
     * A file.
     */
    @com.tagtraum.japlscript.Plural("files")
    @com.tagtraum.japlscript.Code("file")
    @com.tagtraum.japlscript.Name("file")
    @com.tagtraum.japlscript.Inherits("item")
    public interface File extends com.tagtraum.japlscript.Reference, Item {

        com.tagtraum.japlscript.types.TypeClass CLASS = com.tagtraum.japlscript.types.TypeClass.getInstance("file", "\u00abclass file\u00bb", null, Item.CLASS);

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

        /**
         * Returns all properties for an instance of this class.
         *
         * @return Map containing all properties
         */
        Map<String, Object> getProperties();

    }

    /**
     *
     */
    @com.tagtraum.japlscript.Code("priv")
    @com.tagtraum.japlscript.Name("priv")
    public enum Priv implements com.tagtraum.japlscript.JaplEnum, com.tagtraum.japlscript.JaplType<Priv> {

        READ_ONLY("read only", "read", null),
        READ_WRITE("read write", "rdwr", null),
        WRITE_ONLY("write only", "writ", null),
        NONE("none", "none", null);

        private final String name;
        private final String code;
        private final String description;

        private Priv(final String name, final String code, final String description) {
            this.name = name;
            this.code = code;
            this.description = description;
        }

        @Override
        public java.lang.String getName() { return this.name;}

        @Override
        public java.lang.String getCode() { return this.code;}

        @Override
        public java.lang.String getDescription() { return this.description;}

        /**
         * Return the correct enum member for a given string/object reference.
         */
        @Override
        public Priv _parse(final java.lang.String objectReference, final java.lang.String applicationReference) {
            if ("read".equals(objectReference) || "read only".equals(objectReference) || "«constant ****read»".equals(objectReference)) return READ_ONLY;
            else if ("rdwr".equals(objectReference) || "read write".equals(objectReference) || "«constant ****rdwr»".equals(objectReference)) return READ_WRITE;
            else if ("writ".equals(objectReference) || "write only".equals(objectReference) || "«constant ****writ»".equals(objectReference)) return WRITE_ONLY;
            else if ("none".equals(objectReference) || "none".equals(objectReference) || "«constant ****none»".equals(objectReference)) return NONE;
            else throw new java.lang.IllegalArgumentException("Enum " + name + " is unknown.");
        }

        @Override
        public java.lang.String _encode(Object japlEnum) {
            return ((com.tagtraum.japlscript.JaplEnum)japlEnum).getName();
        }

        @Override
        public java.lang.Class<Priv> _getInterfaceType() {
            return Priv.class;
        }

    }

}
