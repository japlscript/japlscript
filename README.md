[![LGPL 2.1](https://img.shields.io/badge/License-LGPL_2.1-blue.svg)](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/japlscript-complete/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/japlscript-complete)
[![Build and Test](https://github.com/hendriks73/japlscript/workflows/Build%20and%20Test/badge.svg)](https://github.com/hendriks73/japlscript/actions)
[![CodeCov](https://codecov.io/gh/hendriks73/japlscript/branch/main/graph/badge.svg?token=H98FM0SKQL)](https://codecov.io/gh/hendriks73/japlscript/branch/main)


# JaplScript

*JaplScript* is an imperfect bridge layer between Java and AppleScript.
It was created to serve a specific purpose and not to be a grand powerful library.

The overall approach is to

- read `.sdef` files (exported with macOS's *Script Editor*)
- generate annotated Java interfaces for the defined AppleScript classes
- compile the interfaces before runtime
- use the interfaces at runtime as if they were Java objects


## Installation
               
JaplScript is released via [Maven](https://maven.apache.org).
You can install it via the following dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.tagtraum</groupId>
        <artifactId>japlscript-runtime</artifactId>
    </dependency>
    <dependency>
        <groupId>com.tagtraum</groupId>
        <artifactId>japlscript-generator</artifactId>
        <!-- the generator is not necessary during runtime -->
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Ant-based Interface Generation

The generator class is implemented as [Ant](https://ant.apache.org) task,
so you can use it from any Ant file like this:

```xml
<project default="generate.interfaces">
    <target name="generate.interfaces">
        <taskdef name="japlscript"
                 classname="com.tagtraum.japlscript.generation.Generator"
                 classpathref="your.reference"/>
        <japlscript application="Music"
                    sdef="Music.sdef"
                    out="src/generated-sources"
                    packagePrefix="com.apple.music">
            <excludeclass name="rgb color"/>
        </japlscript>
    </target>
</project>
```

The attribute `application` describes the application's name as used in a
regular AppleScript `tell` command (which implies you can also use the bundle
name).

Note that the sample above uses an `<excludeclass/>` tag, which simply means that
JaplScript should not generate a Java interface for the given AppleScript 
class or type (in this example: `rgb color`).

                  
## Maven-based Interface Generation

From Maven, you can run a suitable Ant file using the
[maven-antrun-plugin](https://maven.apache.org/plugins/maven-antrun-plugin/). If you do so,
and have declared JaplScript as a dependency, you can set `classpathref="maven.compile.classpath"`
when you define the `japlscript` code generator task.

Sample Ant file `japlscript.xml`:

```xml
<project default="generate.interfaces">
    <target name="generate.interfaces">
        <taskdef name="japlscript"
                 classname="com.tagtraum.japlscript.generation.Generator"
                 classpathref="maven.compile.classpath"/>
        <japlscript application="Music"
                    sdef="Music.sdef"
                    out="${project.build.directory}/generated-sources/main/java"
                    packagePrefix="com.apple.music">
            <excludeclass name="rgb color"/>
        </japlscript>
    </target>
</project>
```

Sample Maven `pom.xml` excerpt:

```xml
<plugin>
    <artifactId>maven-antrun-plugin</artifactId>
    <executions>
        <execution>
            <configuration>
                <target>
                    <!--
                    pass project.build.directory to ant, so you can use it when
                    specifying the output folder, which could be
                    ${project.build.directory}/generated-sources/main/java 
                     -->
                    <property name="project.build.directory" value="${project.build.directory}" />
                    <ant antfile="japlscript.xml" inheritRefs="true" />
                </target>
                <sourceRoot>${project.build.directory}/generated-sources/main/java</sourceRoot>
            </configuration>
            <phase>generate-sources</phase>
            <goals>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Custom Type Mappings                                   

To introduce custom mappings from AppleScript classes to your own classes,
you can use type mappings using the `<typemapping/>` tag in your Ant file,
for example:
                                    
```xml
<project default="generate.interfaces">
    <target name="generate.interfaces">
        <taskdef name="japlscript"
                 classname="com.tagtraum.japlscript.generation.Generator"
                 classpathref="maven.compile.classpath"/>
        <japlscript application="Music"
                    sdef="Music.sdef"
                    out="${project.build.directory}/generated-sources/main/java"
                    packagePrefix="com.apple.music">
            
            <!-- mapping from "file" to "com.apple.finder.File" -->
            <typemapping applescript="file" java="com.apple.finder.File"/>
            
        </japlscript>
    </target>
</project>
```

Note that your custom Java types should implement the interface
`com.tagtraum.japlscript.Codec` to support encoding/decoding from
an AppleScript object (specifier).

If your custom type is not a primitive, you probably also want to
implement `com.tagtraum.japlscript.Reference`.
                             

## Usage
                           
To use the generated code, do something like this:

```java
// if you have generated classes for the Music.app
com.apple.music.Application app = com.apple.music.Application.getInstance();

// then use app, for example, toggle playback (if a track is in the player)
app.playpause();
```
                
## Java Module

JaplScript is shipped as a Java module
(see [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System))
with the name `tagtraum.japlscript`.
                                  
Note that module support is also possible for the generated code.
If you specify a module name during generation, the generated code will also
be a module. E.g.:

```xml
<project default="generate.interfaces">
    <target name="generate.interfaces">
        <taskdef name="japlscript"
                 classname="com.tagtraum.japlscript.generation.Generator"
                 classpathref="maven.compile.classpath"/>
        <japlscript application="Music"
                    module="tagtraum.music"
                    sdef="Music.sdef"
                    out="${project.build.directory}/generated-sources/main/java"
                    packagePrefix="com.apple.music">
        </japlscript>
    </target>
</project>
```

This will create an appropriate `module-info.java` file exporting the module
named `tagtraum.music`.

Note that the generator requires Ant, which has not yet transitioned
to modules, which may lead to problems. 


## Known Shortcomings

Note that the generated interfaces may not always be perfect. This is especially
true for complex AppleScript types and the cardinality of command return types.
In some cases, you may need to fix the generated Java interface manually
(e.g. the cardinality of the return type of the Music.app's `search`-command).
                      

## Additional Resources

- [AppleScript Language Guide](https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/introduction/ASLR_intro.html)