/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.TypeClass;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * TestObjectInvocationHandler.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestObjectInvocationHandler {

    @Test
    public void testGetProperties() throws Throwable {
        final Finder finder = JaplScript.getApplication(Finder.class, "Finder");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(finder);
        final Map<String, Object> properties = (Map<String, Object>)handler.invoke(null, Finder.class.getMethod("getProperties", null), null);
        // NOTE: correct result is limited to what we declare in the Finder test class
        //       this means that we will see warnings for all undeclared properties.
        assertEquals("Finder", properties.get("name"));
        assertTrue(properties.containsKey("typeClass"));
        assertNull(properties.get("typeClass"));
        // TODO: test other props?
    }

    /**
     * Stripped down Finder
     */
    @com.tagtraum.japlscript.Code("capp")
    @com.tagtraum.japlscript.Name("application")
    public interface Finder extends Reference {
        TypeClass CLASS = TypeClass.getInstance("application", "\u00abclass capp\u00bb", null, null);
        Set<java.lang.Class<?>> APPLICATION_CLASSES = new java.util.HashSet<>(java.util.Arrays.asList(Finder.class));

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
