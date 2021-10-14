[![LGPL 2.1](https://img.shields.io/badge/License-LGPL_2.1-blue.svg)](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/japlscript/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/japlscript)
[![Build and Test](https://github.com/japlscript/japlscript/workflows/Build%20and%20Test/badge.svg)](https://github.com/japlscript/japlscript/actions)
[![CodeCov](https://codecov.io/gh/japlscript/japlscript/branch/main/graph/badge.svg?token=H98FM0SKQL)](https://codecov.io/gh/japlscript/japlscript/branch/main)


# JaplScript

*JaplScript* is an imperfect bridge layer between Java and AppleScript.
It was created to serve a specific purpose and not to be a grand powerful library.

The overall approach is to

1) Read `.sdef` files (exported with macOS's *Script Editor*).
2) Generate annotated Java interfaces and enumerations for the defined AppleScript classes.
3) Compile the interfaces/enums before runtime.
4) Use them just like Java objects.


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
                 classname="com.tagtraum.japlscript.generation.GeneratorAntTask"
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
                 classname="com.tagtraum.japlscript.generation.GeneratorAntTask"
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
you can use the `<typemapping/>` tag in your Ant file, for example:
                                    
```xml
<project default="generate.interfaces">
    <target name="generate.interfaces">
        <taskdef name="japlscript"
                 classname="com.tagtraum.japlscript.generation.GeneratorAntTask"
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
[Codec<T>](https://japlscript.github.io/japlscript/com/tagtraum/japlscript/Codec.html) to support encoding/decoding from
an AppleScript object (specifier).

If your custom type is not a primitive, you probably also want to
implement the [Reference](https://japlscript.github.io/japlscript/com/tagtraum/japlscript/Reference.html)
interface.


## Scripting Additions

To generate Java APIs for scripting additions, set the `scriptingAddition`
attribute to `true`. Example:

```xml
<project default="generate.interfaces">
    <target name="generate.interfaces">
        <taskdef name="japlscript"
                 classname="com.tagtraum.japlscript.generation.GeneratorAntTask"
                 classpathref="maven.compile.classpath"/>
        <japlscript application="StandardAdditions"
                    scriptingAddition="true"
                    sdef="StandardAdditions.sdef"
                    out="${project.build.directory}/generated-sources/main/java"
                    packagePrefix="com.apple.macos">
            
        </japlscript>
    </target>
</project>
```

Note that typically the main class for an application is aptly named `Application.class`.
For scripting additions that is not the case—they are called `ScriptingAddition.class`
instead.


## Usage

### Getting Started...
                           
To use the generated code, do something like this:

```java
// if you have generated classes for the Music.app
com.apple.music.Application app = com.apple.music.Application.getInstance();

// then use app, for example, toggle playback (if a track is in the player)
app.playpause();
```

### AppleScript Type System Support

Every JaplScript object that refers to an AppleScript counterpart
implements the interface [Reference](https://japlscript.github.io/japlscript/com/tagtraum/japlscript/Reference.html).
As such, you can `<T> T cast(java.lang.Class<T> klass)` an object to another
Java type that in turn corresponds to another AppleScript type. Note that
type checks may be lazy, i.e. you might not get an exception right away, should
the cast not work.

If you want to check, whether a cast would be legitimate, you can call
`boolean isInstanceOf(TypeClass typeClass)`. A
[TypeClass](https://japlscript.github.io/japlscript/com/tagtraum/japlscript/language/TypeClass.html)
is the Java-side pendant for an AppleScript class. Each of the generated interfaces
exposes its `TypeClass` via it `CLASS` field. For example, if you have an instance of
Java-interface `Track`, you can access `Track.CLASS` to retrieve its AppleScript type.
This means, you could ask an instance of `Track` whether its also an instance of the
sub-class `FileTrack`:

```java
Application application = Application.getInstance();
Track track = application.getCurrrentTrack();
// check, whether the AppleScript object references by track
// is actually a FileTrack and not just a Track. 
if (track.isInstanceOf(FileTrack.CLASS)) {
    // cast the track Java instance to FileTrack. 
    FileTrack fileTrack = track.cast(FileTrack.class);
    ...
}
```

Implicitly, `isInstanceof(..)` uses the method `TypeClass getTypeClass()`, which
lets you find out the actual type of the referenced AppleScript object. This could be
a subtype, of the interface you are currently using.

### Accessing Elements/Collections

In AppleScript, object can have properties and elements. Elements are really just
collections, which can be accessed in JaplScript via generated methods.
Let's assume you have a `PlayList` instance, which has a `Track` elements. Then
JaplScript will generate the following standard methods:

```java
import com.tagtraum.japlscript.Id;

public interface Playlist extends com.tagtraum.japlscript.Reference {

    /**
     * @return an array of all {@link Track}s
     */
    default Track[] getTracks() {
        return getTracks(null);
    }

    /**
     * @param filter AppleScript filter clause without the leading &quot;whose&quot; or &quot;where&quot;
     * @return an array of all {@link Track}s
     */
    Track[] getTracks(java.lang.String filter);

    /**
     * @param index index into the element list
     * @return the {@link Track} at the requested index
     */
    Track getTrack(int index);

    /**
     * @param id id of the item
     * @return the {@link Track} with the requested id
     */
    Track getTrack(Id id);

    /**
     * @return number of all {@link Track}s
     */
    default int countTracks() {
        return countTracks(null);
    }

    /**
     * @param filter AppleScript filter clause without the leading &quot;whose&quot; or &quot;where&quot;
     * @return the number of elements that pass the filter
     */
    int countTracks(String filter);
}
```

They will let you count the tracks and access them in bulk, by index and by id.
Additionally, they let you specify *filters*. These are just little AppleScript
snippets that you would usually use in an AppleScript `where` clause.

For example:

```java
int count = playlist.countTracks("year > 1984");
```

This snippet counts all the tracks in the given playlist that have a year
greater than 1984. Note that this assumes that the `Track` instance has a `year`
property (AppleScript property name, not Java!).
Similar filters can be used in the other provided methods.


### Creating new Objects

Creating new AppleScript objects is sometimes not as straight forward as one might
wish. For example, to create a new playlist in the Apple Music app (or iTunes),
you would use the application's `make()` command.

```java
Application application = Application.getInstance();
UserPlaylist userPlaylist = getApplication().make(UserPlaylist.class);
```

Note that using the Java class here is just a convenience. If you want to
specify additional arguments, like a parent playlist of folder, you would have
to write something like this:

```java
Reference reference = application.make(UserPlaylist.CLASS, someParentPlaylist, null);
UserPlaylist userPlaylist = reference.cast(UserPlaylist.CLASS);
```

### Bulk Accessing Properties

Every JaplScript object has a method `java.util.Map<String, Object> getProperties()`,
which lets you retrieve the object's properties in a convenient `java.util.Map`.
Note that the keys correspond to the Java property names. The advantage of
using `getProperties()` instead of individually accessing properties one by
one is efficiency, since fewer AppleScript calls are needed.


### Sessions

When calling multiple setters in a row, JaplScript will translate each call
to an AppleScript snippet and execute it. This of course inefficient. It may make
more sense to first collect a bunch of calls and then execute them all at once.
You can achieve this kind of behavior by starting a [Session](https://japlscript.github.io/japlscript/com/tagtraum/japlscript/execution/Session.html):

```java
import com.tagtraum.japlscript.execution.Session;

[...]

Application application = Application.getInstance();
// start session for the current thread
Session session = Session.startSession();
// call some setters
application.setThis("this");
application.setThat("that");
application.setOther("other");
// call commit in order to execute the combined AppleScript snippets
session.commit();
```

### Logging

JaplScript uses `java.util.logging`. In order to see what scripts are being executed and when,
just dial up the log level.


### Artificial References

Usually you will be able to obtain Java objects for your AppleScript objects
using the generated interfaces and their methods. But sometimes this can be awkward
and you much rather just want to use an AppleScript snippet. This can easily be done
by using a generic [ReferenceImpl](https://japlscript.github.io/japlscript/com/tagtraum/japlscript/language/ReferenceImpl.html).

To do so you have to understand that each `Reference` consists of two parts:

1. An object reference, describing an object within an application's context
2. An Application reference, describing the application context

So to create a Java object for an arbitrary AppleScript object, you can simply do
something like this:

```java
Application application = Application.getInstance();
final String objectReference = "(first source where kind is library)";
Reference reference = new ReferenceImpl(objectReference, application.getApplicationReference());
// cast to the Java interface that you know fits
Source librarySource = reference.cast(Source.class); 
```

The snippet above allows you to create a Java instance for the first library source of
some application (think *Music.app* or *iTunes*) without executing a single line of
AppleScript. Obviously, `objectReference` could also be some other random snippet
of AppleScript that returns some object.


## Sample Projects

- [JaplSA](https://github.com/japlscript/japlsa) - Java API for AppleScript Standard Additions
- [JaplSE](https://github.com/japlscript/japlse) - Java API for AppleScript System Events
- [Japlphoto](https://github.com/japlscript/japlphoto) - Java API for Apple's Photos app
- [Japlfind](https://github.com/japlscript/japlfind) - Java API for Apple's Finder app
- [Japlcontact](https://github.com/japlscript/japlcontact) - Java API for Apple's Contacts app
- [Obstunes](https://github.com/japlscript/obstunes) - Java API for iTunes 
- [Obstmusic](https://github.com/japlscript/obstmusic) - Java API for Apple's Music app
- [Obstspot](https://github.com/japlscript/obstspot) - Java API for the Spotify app

Have you generated an API stored in your repository? Open a PR to list it here.

Want to have your API repository listed under https://github.com/japlscript, consider
transferring ownership to the *japlscript* GitHub organization.

                
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
                 classname="com.tagtraum.japlscript.generation.GeneratorAntTask"
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
               

## AppleScript Sandbox

Since macOS 10.14 (Mojave), Apple imposed a sandbox on AppleScript. Therefore
you may see dialog boxes requesting authorization to perform certain actions.
After a while, these boxes simply disappear and there does not seem to be an easy
way to authorize your app. In this case, you need to open the system preferences,
navigate to *Security & Privacy*, *Privacy*, and then *Automation*, and make
sure your app is allowed to remote control whatever app you are trying to remote
control (see also [this article](https://blog.beatunes.com/2018/10/beatunes-on-mojave-and-windows-10-dark.html)).

If you are shipping a real app with a UI and not just a command line tool, you
need to customize the sandbox permission dialog. You can do so by adding
the key `NSAppleEventsUsageDescription` to your app bundle's `/Contents/Info.plist`
file. For example: 
       
    [...]
    <key>NSAppleEventsUsageDescription</key>
    <string>SuperMusic uses AppleEvents to access your Music.app library,
            e.g., to set BPM values or create playlists.</string>
    [...]

Apple's documentation for the keyword is [here](https://developer.apple.com/documentation/bundleresources/information_property_list/nsappleeventsusagedescription).


## Known Shortcomings

Note that the generated interfaces may not always be perfect. This is especially
true for complex AppleScript types and the cardinality of command return types.
In some cases, you may need to fix the generated Java interface manually
(e.g. the cardinality of the return type of the Music.app's `search`-command).

There are also issued with generating *all* possible versions of overloaded
AppleScript commands.

Ant really should not be necessary during generation. Instead a simple Maven
plugin should do the job.


## API

You can find the complete [API here](https://japlscript.github.io/japlscript/).


## Additional Resources

- [AppleScript Language Guide](https://developer.apple.com/library/archive/documentation/AppleScript/Conceptual/AppleScriptLangGuide/introduction/ASLR_intro.html)
- [Raw AppleScript Event Codes](https://gist.github.com/ccstone/955a0461d0ba02289b0cef469862ec84)
 