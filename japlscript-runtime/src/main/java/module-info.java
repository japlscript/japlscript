/**
 * JaplScript runtime.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
module tagtraum.japlscript {
    requires transitive tagtraum.japlscript.executor;
    exports com.tagtraum.japlscript;
    exports com.tagtraum.japlscript.language;
}