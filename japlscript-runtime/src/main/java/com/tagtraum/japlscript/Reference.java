/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.language.TypeClass;

/**
 * Reference to an AppleScript object.
 * Each reference instance consists of an {@link #getObjectReference() object part}
 * and an {@link #getApplicationReference() application part}.<br>
 * The {@link #getObjectReference() object part} points to a specific
 * object within an application, e.g. {@code first window}.
 * The {@link #getApplicationReference() object part}
 * points to the scriptable application itself, e.g. {@code Finder}.
 * Together they form a complete
 * <a href="https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/conceptual/ASLR_fundamentals.html#//apple_ref/doc/uid/TP40000983-CH218-SW7">Object Specifier</a>,
 * e.g. {@code first window of application "Finder"}.<br>
 *
 * <p> As mentioned above, in most cases, a reference is an
 * <a href="https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/conceptual/ASLR_fundamentals.html#//apple_ref/doc/uid/TP40000983-CH218-SW7">Object Specifier</a>,
 * and <em>not strictly identical</em> to an
 * <a href="https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/reference/ASLR_classes.html#//apple_ref/doc/uid/TP40000983-CH1g-BBCDJJDE">AppleScript Reference</a>.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see Codec
 * @see <a href="https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/introduction/ASLR_intro.html">AppleScript Language Guide</a>
 */
public interface Reference {

    /**
     * Object reference.
     *
     * @return object reference
     */
    String getObjectReference();

    /**
     * Application reference.
     *
     * @return application reference
     */
    String getApplicationReference();

    /**
     * Cast this object to another AppleScript type.
     *
     * @param klass type to cast to
     * @param <T> target type
     * @return cast object
     */
    <T> T cast(java.lang.Class<T> klass);

    /**
     * Returns the AppleScript <em>runtime</em> class for this object.
     * <p>
     * Note that the runtime may be different from the class corresponding
     * to the Java interface you are currently using as a proxy, i.e.
     * the Java interface may be less specialized.
     *
     * @return class
     */
    @Type("type")
    @Name("class")
    @Code("type")
    @Kind("property")
    TypeClass getTypeClass();

    /**
     * Indicates whether this object is an instance of the given {@link TypeClass}.
     *
     * @param typeClass type class
     * @return true or false
     */
    boolean isInstanceOf(TypeClass typeClass);
}
