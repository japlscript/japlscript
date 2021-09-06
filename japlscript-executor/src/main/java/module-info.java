/**
 * Core classes that allow AppleScript execution from Java.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
module tagtraum.japlscript.executor {
    requires transitive java.logging;
    requires transitive java.desktop;
    exports com.tagtraum.japlscript.execution;
}