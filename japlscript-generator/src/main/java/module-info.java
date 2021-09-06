/**
 * JapleScript code generator.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
module tagtraum.japlscript.generator {
    requires transitive tagtraum.japlscript;
    requires java.xml;
    requires java.compiler;
    requires java.logging;
    requires ant;
    exports com.tagtraum.japlscript.generation;
}