- 3.4.14

  - Updated jacoco-maven-plugin to version 0.8.13
  - Fixed StandardAddition tests for AARCH64-based systems
  - Updated GitHub Action plugins
  - Fixed `getTypeClass()` invocation in generated proxies.


- 3.4.13

  - Use chevron codes instead of clear names for properties


- 3.4.12

  - Modified build scripts to use `actions/setup-java@v2`
  - Renamed `Session.getSession()` to `Session.get()`
  - Removed unicode escaping, as it is unnecessary since OS X 10.5 (Leopard)


- 3.4.11

  - Improved using most specific Java class when creating dynamic proxies
  - Added signature to packaged `dylib` ([#5](https://github.com/japlscript/japlscript/issues/5))
  - Fixed, use `null` in records, when encountering a `missing value`
 
 
- 3.4.10

  - Fixed `POSIX file` parsing issue.

 
- 3.4.9
 
  - Moved GitHub repository to https://github.com/japlscript/japlscript 

 
- 3.4.8

  - Improved documentation in `README.md`
  - Improved some javadoc comments for central classes
  - Modified the generated AppleScript that accesses elements by index
  - Added `java.nio.file.Path` constructor to `Tdta.java`
  - Added `@Documented` to annotations, so that they are visible in javadoc 
  - Fixed `TestDateParser` for Spanish AM/PM strings with non-breaking space
  - Fixed bad "To get started..." info in generated `package-info.java`
  - Fixed `TypeClass` intern failure when calling `getProperties()`
  - Fixed missing first char in boolean property names when calling `getProperties()`


- 3.4.7

  - Fixed `getInstance()` error introduced in 3.4.6

 
- 3.4.6

  - Follow Java conventions and name boolean getters "is"XXX
  - Fixed Chevron parser to allow spaces in codes
  - Modified/fixed capitalization rules for Java class and method names
  - Added descriptive parameter names for setters
  - Added support for record types
  - Added support for scripting additions 
  - Added escaping of special HTML characters when generating
    javadoc comments
  - Modified alias code to map them to POSIX files (native execution only) 


- 3.4.5

  - Fixed handling of missing plurals (by appending an 's')
  - Fixed casting error for primitive arrays as return types


- 3.4.4

  - Added generation of `package-info.java`
  - Added `@author` tag to generated `.java` files
  - Added generation of aggregated Javadocs to GitHub Pages
  - Fixed project URL


- 3.4.3

  - Fixed per class hierarchy lookup of properties, when
    translating from AppleScript to Java.
  - Fixed several generator issues for overloaded commands
  - Renamed package `com.tagtraum.japlscript.types` to `com.tagtraum.japlscript.language`
  - Renamed Ant task to `com.tagtraum.japlscript.generation.GeneratorAntTask`
  - Added StandardAdditions and SystemEvents to generator tests
  - Added enumerations to `Application.APPLICATION_CLASSES`
  - Added `_getAppleScriptTypes()` to `Codec`
  - Added AppleScript language type `Short`


- 3.4.2
  
  - Fixed Maven Central badge
  - Reduced log chatter


- 3.4.1

  - Generated `module-info.java` now contains transitive dependency 
    on `tagtraum.japlscript`
  - Renamed `JaplType` to `Codec` and changed its method names


- 3.4.0

  - Refactored package structure and removed `japlscript-complete` module
  - Added `application` attribute to Ant generator task
  - Added `getInstance()` method to generated Application classes
  - Added ability to automatically generate `module-info.java`
  - Added Maven 3.6.0 or later requirement
  - Added way of forcing use of osascript command line tool
  - Updated Javadoc JDK link to Java 9
  - Completed JUnit 5 migration to org.junit.jupiter.api
  - Fixed issue in native code when returning a reference which contains a property
  - Improved some javadocs

 
- 3.3.0

  - Moved to Java 9 and modules
  - Moved from SLF4J/Log4J to java.util.logging
  - Moved to JUnit 5.7.2


- 3.2.1
  - Switched to JaCoCo for measuring test coverage
  - Improved test code coverage
  - Changed class lookup precedence during generation
  - Moved parameter annotations for commands to method parameters list
  - Use default methods for unfiltered element collections
  - Added basic generation code for type-values
  - Removed generator code for setting elements with index
  - Consolidated default runtime type mapping for standard types
  - Refactored Generator to disentangle what we need to generate and how
  - Fixed (call to) native library loader
  - Added initial native support for records 
  - Added generated getProperties() method 


- 3.2.0

  - Moved source code repo to GitHub
  - Enabled GitHub Actions for CI
  - Added code coverage test
  - Added support for ARM64/aarch64
  - Re-enabled deployment to Maven Central


- 3.1.9

  - Ensure a session cannot accidentally be executed twice.
  - Fixed DateParser tests for Java 11.


- 3.1.8

  - Added ExecutionListener to allow for UI feedback (when a script is actually executed).
  - 32 bit no longer supported.


- 3.1.7

  - Fixed issues with JaplScriptFile (bad Alias assumption).
  - Fixed several Javadoc warnings.


- 3.1.6

  - Ensure that null can be cast to anything.


- 3.1.5

  - Improved formatting of generated code.
  - Fixed issues with special sandbox elements.
  - Added raw data as standard type.
  - Updated packaged sdef.dtd.


- 3.1.4

  - Fixed library loading issues when the classpath contains a + char.


- 3.1.3

  - Fixed date handling.
  - Updated Maven plugins.
  - Updated slf4j.
  - Updated OS X SDK.
  - Updated source code encoding in pom.xml.


- 3.1.2

  - Added conversion of \uXXX sequences to unicode in JaplScriptExceptions.


- 3.1.1

  - Moved from jnilib to dylib file extension.


- 3.1.0

  - Moved source to Java 7.
  - Generated source code now UTF-8 encoded.


- 3.0.7

  - Date support.


- 3.0.6

  - Fixed session timeout.


- 3.0.5

  - Added exception for unresolvable alias.
  - Replaced deprecated method calls.


- 3.0.4

  - Added global and session aspects concept.


- 3.0.3


- 3.0.2

  - Re-organized native builds to get rid of XCode dependency.


- 3.0.1

  - Several fixes for updates XML sdef format.


- 3.0.0

  - First public release.