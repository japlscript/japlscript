- 3.2.1
  - Switched to JaCoCo for measuring test coverage

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