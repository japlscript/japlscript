/**
 * module-info.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
module tagtraum.japlscript {
    requires java.desktop;
    requires java.xml;
    requires java.compiler;
    requires java.logging;
    requires static ant;
    exports com.tagtraum.japlscript;
    exports com.tagtraum.japlscript.types;
}