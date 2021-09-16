/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.generation;

import com.tagtraum.japlscript.*;
import com.tagtraum.japlscript.language.TypeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generates Interface source files for an <code>.sdef</code> file.
 * Can be used as <a href="http://ant.apache.org/">Ant</a> task.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Generator {

    private static final String SDEF_DTD = "file://localhost/System/Library/DTDs/sdef.dtd";
    private Path sdef;
    private String packagePrefix = "com.tagtraum.japlscript";
    private String application;
    private String module;
    private Path out = Paths.get(".");
    private final Map<String, String> customTypeMapping = new HashMap<>();
    private final Set<String> excludeClassSet = new HashSet<>();
    private Map<String, List<Element>> classMap;
    private Map<String, List<Element>> enumerationMap;
    private boolean generateElementSetters = false;
    private BiConsumer<String, Level> logMessageConsumer = (message, logLevel) -> Logger.getLogger(Generator.class.getName()).log(logLevel, message);

    public BiConsumer<String, Level> getLogMessageConsumer() {
        return logMessageConsumer;
    }

    public void setLogMessageConsumer(final BiConsumer<String, Level> logMessageConsumer) {
        this.logMessageConsumer = logMessageConsumer;
    }

    private void log(final String message, final Level level) {
        getLogMessageConsumer().accept(message, level);
    }

    private void log(final String message) {
        log(message, Level.INFO);
    }

    /**
     * Indicates whether element setters are generated or not.
     * This is {@code false} by default, as the invocation code
     * in {@link ObjectInvocationHandler} is not certain to work.
     *
     * @return true or false
     */
    public boolean isGenerateElementSetters() {
        return generateElementSetters;
    }

    /**
     * Turn generation of element setters on or off.
     *
     * @param generateElementSetters true or false
     * @see #isGenerateElementSetters()
     */
    public void setGenerateElementSetters(final boolean generateElementSetters) {
        final boolean oldGenerateElementSetters = this.generateElementSetters;
        this.generateElementSetters = generateElementSetters;
        if (oldGenerateElementSetters != generateElementSetters && generateElementSetters) {
            log("You have turned on the generation of element setters. " +
                    "Please note that element setters may not work at all or as expected.", 
                Level.WARNING);
        }
    }

    /**
     * Lets you configure a custom mapping from AppleScript types
     * to Java types.
     *
     * @param typeMapping type mapping
     */
    public void addConfiguredTypeMapping(final TypeMapping typeMapping) {
        customTypeMapping.put(typeMapping.getApplescript(), typeMapping.getJava());
    }

    /**
     * Retrieve a custom mapping from AppleScript type to a Java type.
     *
     * @param applescriptType AppleScript type
     * @return corresponding Java type or {@code null}
     */
    public String getConfiguredTypeMapping(final String applescriptType) {
        return this.customTypeMapping.get(applescriptType);
    }

    /**
     *
     * @param excludeClass excluded class
     */
    public void addConfiguredExcludeClass(final ExcludeClass excludeClass) {
        excludeClassSet.add(excludeClass.getName());
    }

    public boolean isClassExcluded(final String classname) {
        return excludeClassSet.contains(classname);
    }

    public Path getSdef() {
        return sdef;
    }

    public void setSdef(final File sdef) {
        setSdef(sdef.toPath());
    }

    public void setSdef(final Path sdef) {
        this.sdef = sdef;
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    /**
     *
     * @param packagePrefix prefix for generated package names
     */
    public void setPackagePrefix(final String packagePrefix) {
        if (packagePrefix.endsWith(".")) this.packagePrefix = packagePrefix.substring(0, packagePrefix.length() - 1);
        else this.packagePrefix = packagePrefix;
    }

    public String getApplication() {
        return application;
    }

    /**
     * Application name or bundle that would be used in an AppleScript call.
     * E.g. "iTunes".
     *
     * @param application application name
     */
    public void setApplication(final String application) {
        this.application = application;
    }

    public String getModule() {
        return module;
    }

    /**
     * Name of the generated JPMS module.
     *
     * @param module module name name
     */
    public void setModule(final String module) {
        this.module = module;
    }

    public Path getOut() {
        return out;
    }

    public void setOut(final File out) {
        this.out = out.toPath();
    }

    public void setOut(final Path out) {
        this.out = out;
    }

    private String getPackageName() {
        String filename = sdef.getFileName().toString();
        final int lastDot = filename.lastIndexOf('.');
        if (lastDot != -1) filename = filename.substring(0, lastDot);
        return toPackageName(filename);
    }

    /**
     * Generates JaplScript classes/interfaces.
     *
     * @throws ParserConfigurationException parser issues
     * @throws IOException IO issues
     * @throws SAXException XML parsing issues
     */
    public void generate() throws ParserConfigurationException, IOException, SAXException {
        log("Generating sources...", Level.INFO);
        log("Application: " + (application == null ? "<not specified>" : application), Level.INFO);
        log("Sdef: " + sdef, Level.INFO);
        log("Generation output path: " + out, Level.INFO);
        log("Package prefix: " + packagePrefix, Level.INFO);
        log("Package: " + getPackageName(), Level.INFO);
        log("Module: " + (module == null ? "<not specified>" : module), Level.INFO);

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(final String publicId, final String systemId)  {
                if (SDEF_DTD.equals(systemId)) {
                    final InputStream sdefDTD = getClass().getResourceAsStream("sdef.dtd");
                    assert sdefDTD != null : "Failed to find sdef.dtd";
                    return new InputSource(sdefDTD);
                }
                return null;
            }
        });
        final Document sdefDocument = documentBuilder.parse(this.sdef.toFile());

        buildClassMap(sdefDocument);
        buildEnumerationMap(sdefDocument);

        final List<ClassSignature> classSignatures = createClasses();
        final List<ClassSignature> enumSignatures = createEnumerations(sdefDocument);

        // add set of all classes and properties to application class
        final ClassSignature applicationClassSignature = classSignatures.stream()
            .filter(ClassSignature::isApplicationClass)
            .findFirst()
            .orElse(null);
        if (applicationClassSignature != null) {
            final String fqcn = Stream.of(classSignatures, enumSignatures)
                .flatMap(Collection::stream)
                .map(classSignature -> classSignature.getFullyQualifiedClassName() + ".class")
                .collect(Collectors.joining(", ", Set.class.getName() + "<" + Class.class.getName() + "<?>> APPLICATION_CLASSES = new " + HashSet.class.getName() + "<>(" + Arrays.class.getName() + ".asList(", "))"));
            final FieldSignature applicationClasses = new FieldSignature(fqcn, "All classes belonging to this application.");
            applicationClassSignature.add(applicationClasses);

            if (application != null) {
                writeGetInstanceMethod(applicationClassSignature);
            } else {
                log("No application name or bundle set. Won't generate getInstance() method for "
                    + applicationClassSignature.getFullyQualifiedClassName());
            }
        }

        writeClasses(classSignatures);
        writeClasses(enumSignatures);

        if (module != null) {
            writeModuleInfo();
        } else {
            log("No Java module name was specified. You won't be able to use " +
                "the generated code as module, unless you add a custom module-info.java file.");
        }
    }

    private void writeModuleInfo() throws IOException {
        final Path moduleInfoPath = out.resolve("module-info.java");
        Files.write(moduleInfoPath, Arrays.asList(
            "/**",
            " * Module info for " + sdef.getFileName().toString() + ", generated by JaplScript.",
            " */",
            "module " + module + " {",
            "    requires transitive tagtraum.japlscript;",
            "    exports " + getPackageName() + ";",
            "}"), UTF_8);
    }

    private void writeGetInstanceMethod(final ClassSignature applicationClassSignature) {
        final MethodSignature getInstanceMethod = new MethodSignature("getInstance");
        final String appFullyQualifiedClassName = applicationClassSignature.getFullyQualifiedClassName();
        getInstanceMethod.setReturnType(appFullyQualifiedClassName);
        getInstanceMethod.setReturnTypeDescription("instance");
        getInstanceMethod.setVisibility("static");
        getInstanceMethod.setDescription("Returns an instance for application " + application + ".");
        getInstanceMethod.setBody("return " + JaplScript.class.getName() + ".getApplication("
            + appFullyQualifiedClassName + ".class, \"" + application + "\");");
        applicationClassSignature.add(getInstanceMethod);
    }

    private void writeClasses(final List<ClassSignature> classes) throws IOException {
        for (final ClassSignature classSignature : classes) {
            final Path classFile = createClassFile(classSignature.getFullyQualifiedClassName());
            Files.createDirectories(classFile.getParent());
            try (final BufferedWriter writer = Files.newBufferedWriter(classFile, UTF_8)) {
                writer.write(classSignature.toString());
            }
        }
    }

    private List<ClassSignature> createClasses() {
        final List<ClassSignature> classes = new ArrayList<>();
        for (Map.Entry<String, List<Element>> entry : classMap.entrySet()) {
            if (!isClassExcluded(entry.getKey())) {
                final ClassSignature classSignature = createClass(entry.getValue());
                classes.add(classSignature);
            }
        }
        return classes;
    }

    private List<ClassSignature> createEnumerations(final Document sdefDocument) {
        final List<ClassSignature> enums = new ArrayList<>();
        final NodeList enumerations = sdefDocument.getElementsByTagName("enumeration");
        final int enumerationsLength = enumerations.getLength();
        for (int j = 0; j < enumerationsLength; j++) {
            final Element enumeration = (Element) enumerations.item(j);
            final ClassSignature enumSig = createEnumeration(enumeration);
            enums.add(enumSig);
        }
        return enums;
    }

    private void buildEnumerationMap(final Document sdefDocument) {
        final NodeList enumerations = sdefDocument.getElementsByTagName("enumeration");
        final int enumerationsLength = enumerations.getLength();
        enumerationMap = new HashMap<>();
        for (int j = 0; j < enumerationsLength; j++) {
            final Element enumeration = (Element) enumerations.item(j);
            final String enumerationName = enumeration.getAttribute("name");
            List<Element> list = enumerationMap.computeIfAbsent(enumerationName, k -> new ArrayList<>());
            list.add(enumeration);
        }
    }

    private void buildClassMap(final Document sdefDocument) {
        classMap = new HashMap<>();

        final NodeList classes = sdefDocument.getElementsByTagName("class");
        final int classesLength = classes.getLength();
        for (int j = 0; j < classesLength; j++) {
            final Element klass = (Element) classes.item(j);
            final String className = klass.getAttribute("name");
            List<Element> list = classMap.computeIfAbsent(className, k -> new ArrayList<>());
            list.add(klass);
        }
        final NodeList extensions = sdefDocument.getElementsByTagName("class-extension");
        final int extensionsLength = extensions.getLength();
        for (int j = 0; j < extensionsLength; j++) {
            final Element klass = (Element) extensions.item(j);
            final String className = klass.getAttribute("extends");
            List<Element> list = classMap.computeIfAbsent(className, k -> new ArrayList<>());
            list.add(klass);
        }

        final NodeList valueTypes = sdefDocument.getElementsByTagName("value-type");
        final int valueTypesLength = valueTypes.getLength();
        for (int j = 0; j < valueTypesLength; j++) {
            final Element valueType = (Element) valueTypes.item(j);
            final String valueTypeName = valueType.getAttribute("name");
            // *only* add value-types, if they are not covered yet by standard Java types
            // we do this, because value-type don't really have functionality anyway.
            final String standardJavaType = JaplScript.getStandardJavaType(valueTypeName);
            if (standardJavaType == null) {
                List<Element> list = classMap.computeIfAbsent(valueTypeName, k -> new ArrayList<>());
                list.add(valueType);
            }
        }

        if (!classMap.containsKey("application")) {
            log("SDEF does not contain an application class. Adding artificial Application class.", Level.WARNING);
            final Element application = sdefDocument.createElement("class");
            application.setAttribute("name", "application");
            classMap.put("application", List.of(application));
        }
    }

    private ClassSignature createEnumeration(final Element enumeration) {
        final String className = enumeration.getAttribute("name");
        final String javaClassName = getJavaType(className);

        final ClassSignature enumSig = new ClassSignature("enum", javaClassName, getPackageName(), enumeration.getAttribute("description"));
        enumSig.addImplements(JaplEnum.class.getName());
        enumSig.addImplements(Codec.class.getName() + "<" + javaClassName + ">");
        final String codeAttribute = enumeration.getAttribute("code");
        if (!isNullOrEmpty(codeAttribute))
            enumSig.add(new AnnotationSignature(Code.class, "\"" + codeAttribute + "\""));
        if (!isNullOrEmpty(className))
            enumSig.add(new AnnotationSignature(Name.class, "\"" + className + "\""));

        // enumerators
        final NodeList enumerators = enumeration.getElementsByTagName("enumerator");
        final Set<String> enumeratorNames = new HashSet<>();
        for (int i = 0; i < enumerators.getLength(); i++) {
            final Element enumerator = (Element) enumerators.item(i);
            final String name = enumerator.getAttribute("name");
            if (enumeratorNames.contains(name)) {
                log("Enumeration " + javaClassName + "/" + className + " contains a duplicate enumerator: " + name, Level.SEVERE);
            } else {
                enumeratorNames.add(name);
                final String n = enumerator.getAttribute("name");
                final String code = enumerator.getAttribute("code");
                final String description;
                if (isNullOrEmpty(enumerator.getAttribute("description"))) {
                    description = "null";
                }
                else {
                    description = "\"" + enumerator.getAttribute("description") + "\"";
                }
                final String javaName = Identifiers.toJavaConstant(name);
                enumSig.add(new EnumSignature(javaName, "\"" + n + "\"", "\"" + code + "\"", description));
            }
        }
        if (!isNullOrEmpty(codeAttribute) && !isNullOrEmpty(className)) {
            enumSig.add(new FieldSignature("public static final " + TypeClass.class.getName() + " CLASS = new " + TypeClass.class.getName() + "(\""
                + className + "\", \"\\u00abclass " + codeAttribute + "\\u00bb\", Application.class, null)"));
        }
        enumSig.add(new FieldSignature("private final String name"));
        enumSig.add(new FieldSignature("private final String code"));
        enumSig.add(new FieldSignature("private final String description"));

        final MethodSignature constructor = new MethodSignature(javaClassName);
        constructor.setVisibility("private");
        constructor.add(new ParameterSignature("name", "long name", String.class.getName()));
        constructor.add(new ParameterSignature("code", "AppleScript four-letter code", String.class.getName()));
        constructor.add(new ParameterSignature("description", "description", String.class.getName()));
        constructor.setBody("this.name = name;\n" +
            "    this.code = code;\n" +
            "    this.description = description;");
        //final String name, final String code, final String description
        enumSig.add(constructor);

        final MethodSignature getName = new MethodSignature("getName");
        getName.setVisibility("public");
        getName.setReturnType(String.class.getName());
        getName.setReturnTypeDescription("long name");
        getName.setBody("return this.name;");
        getName.add(new AnnotationSignature(Override.class));
        enumSig.add(getName);

        final MethodSignature getCode = new MethodSignature("getCode");
        getCode.setVisibility("public");
        getCode.setReturnType(String.class.getName());
        getCode.setReturnTypeDescription("AppleScript four-letter code");
        getCode.setBody("return this.code;");
        getCode.add(new AnnotationSignature(Override.class));
        enumSig.add(getCode);

        final MethodSignature getDescription = new MethodSignature("getDescription");
        getDescription.setVisibility("public");
        getDescription.setReturnType(String.class.getName());
        getDescription.setReturnTypeDescription("description");
        getDescription.setBody("return this.description;");
        getDescription.add(new AnnotationSignature(Override.class));
        enumSig.add(getDescription);

        final MethodSignature _decode = new MethodSignature("_decode");
        _decode.setVisibility("public");
        _decode.add(new ParameterSignature("objectReference", "object reference", String.class.getName()));
        _decode.add(new ParameterSignature("applicationReference", "application reference", String.class.getName()));
        _decode.setDescription("Return the correct enum member for a given string/object reference.");
        _decode.setReturnType(javaClassName);
        _decode.setReturnTypeDescription("description");
        final StringBuilder parseSB = new StringBuilder();
        for (int i = 0; i < enumerators.getLength(); i++) {
            final Element enumerator = (Element) enumerators.item(i);
            final String name = enumerator.getAttribute("name");
            final String code = enumerator.getAttribute("code");
            final String javaName = Identifiers.toJavaConstant(name);
            if (i != 0) parseSB.append("    else ");
            parseSB.append("if (\"" + code + "\".equals(objectReference) || \"" + name + "\".equals(objectReference) || \"\u00abconstant ****"
                + code + "\u00bb\".equals(objectReference)) return " + javaName + ";\n");
        }
        parseSB.append("    else throw new ")
            .append(IllegalArgumentException.class.getName())
            .append("(\"Enum \" + name + \" is unknown.\");");

        _decode.setBody(parseSB.toString());
        _decode.add(new AnnotationSignature(Override.class));
        enumSig.add(_decode);

        final MethodSignature _encode  = new MethodSignature("_encode");
        _encode.setVisibility("public");
        _encode.setReturnType(String.class.getName());
        _encode.setReturnTypeDescription("Encode enum as AppleScript string");
        _encode.add(new ParameterSignature("japlEnum", JaplEnum.class.getSimpleName() + " instance", Object.class.getName()));
        _encode.setBody("return ((" + JaplEnum.class.getName() + ")japlEnum).getName();");
        _encode.add(new AnnotationSignature(Override.class));
        enumSig.add(_encode);

        final MethodSignature _getJavaType  = new MethodSignature("_getJavaType");
        _getJavaType.setVisibility("public");
        _getJavaType.setReturnType("java.lang.Class<" + javaClassName + ">");
        _getJavaType.setReturnTypeDescription("Java class used by {@link #_decode(String, String)}");
        _getJavaType.setBody("return " + javaClassName + ".class;");
        _getJavaType.add(new AnnotationSignature(Override.class));
        enumSig.add(_getJavaType);


        final MethodSignature _getAppleScriptTypes  = new MethodSignature("_getAppleScriptTypes");
        _getAppleScriptTypes.setVisibility("public");
        _getAppleScriptTypes.setReturnType(TypeClass.class.getName() + "[]");
        _getAppleScriptTypes.setReturnTypeDescription("AppleScript classes that may be decoded with {@link #_decode(String, String)}");
        _getAppleScriptTypes.setBody("return new " + TypeClass.class.getName() + "[]{CLASS};");
        _getAppleScriptTypes.add(new AnnotationSignature(Override.class));
        enumSig.add(_getAppleScriptTypes);

        // TypeClass[] _getAppleScriptTypes();

        return enumSig;
    }

    private ClassSignature createClass(final List<Element> classList) {
        final Element klass = classList.get(0);
        final String className = klass.getAttribute("name") != null && !klass.getAttribute("name").isEmpty() ? klass.getAttribute("name") : klass.getAttribute("extends");
        final String javaClassName = Identifiers.toCamelCaseClassName(className);

        final ClassSignature classSignature = new ClassSignature("interface", javaClassName, getPackageName(), toJavadocDescription(klass.getAttribute("description")));
        String code = "null";
        String typeSuperClass = null;
        final String superClass = klass.getAttribute("inherits");

        if (!isNullOrEmpty(klass.getAttribute("plural")))
            classSignature.add(new AnnotationSignature(Plural.class, "\"" + klass.getAttribute("plural") + "\""));
        if (!isNullOrEmpty(klass.getAttribute("code")))
            code = "\"\\u00abclass " + klass.getAttribute("code") + "\\u00bb\"";
            classSignature.add(new AnnotationSignature(Code.class, "\"" + klass.getAttribute("code") + "\""));
        if (!isNullOrEmpty(className))
            classSignature.add(new AnnotationSignature(Name.class, "\"" + className + "\""));
        if (!isNullOrEmpty(superClass))
            classSignature.add(new AnnotationSignature(Inherits.class, "\"" + superClass + "\""));

        classSignature.addExtends(Reference.class.getName());
        if (!isNullOrEmpty(superClass) && !superClass.equals(className)) {
            // TODO: package name for super class....
            final String additionalSuperClass = getJavaType(superClass);
            if (!Reference.class.getName().equals(additionalSuperClass)) {
                classSignature.addExtends(additionalSuperClass);
                typeSuperClass = additionalSuperClass + ".CLASS";
            }
        }

        final String typeClassField = TypeClass.class.getName()
            + " CLASS = new " + TypeClass.class.getName() + "(\"" + className + "\", " + code + ", Application.class, " + typeSuperClass + ")";
        classSignature.add(new FieldSignature(typeClassField, null));

//        final String propertiesField = Set.class.getName() + "<" + Property.class.getName() + "> PROPERTIES = " + Property.class.getName() + ".fromAnnotations(" + javaClassName + ".class)";
//        classSignature.add(new FieldSignature(propertiesField, null));

        final List<MethodSignature> methods = new ArrayList<>();

        if (classSignature.isApplicationClass()) {
            // commands
            methods.addAll(createAllCommandMethods(klass.getOwnerDocument()));
        }

        for (final Element classElement : classList) {
            // elements
            final NodeList elements = classElement.getElementsByTagName("element");
            for (int i = 0; i < elements.getLength(); i++) {
                final Element element = (Element) elements.item(i);
                methods.addAll(createElementMethods(element));
            }
            // properties
            final NodeList properties = classElement.getElementsByTagName("property");
            for (int i = 0; i < properties.getLength(); i++) {
                final Element property = (Element) properties.item(i);
                methods.addAll(createPropertyMethods(property, true));
            }
        }

        methods.add(createPropertiesMethod());

        for (final MethodSignature method : methods) {
            if (!classSignature.contains(method)) {
                classSignature.add(method);
            } else {
                log("Skipping " + method + " because it is already declared.");
            }
        }

        return classSignature;
    }

    private MethodSignature createPropertiesMethod() {
        final MethodSignature methodSignature = new MethodSignature("getProperties");
        methodSignature.setReturnTypeDescription("Map containing all properties");
        methodSignature.setReturnType(Map.class.getName() + "<String, Object>");
        methodSignature.setDescription("Returns all properties for an instance of this class.");
        return methodSignature;
    }

    private List<MethodSignature> createAllCommandMethods(final Document document) {
        final List<MethodSignature> methods = new ArrayList<>();

        final NodeList commands = document.getElementsByTagName("command");
        for (int i = 0; i < commands.getLength(); i++) {
            final Element command = (Element) commands.item(i);
            methods.addAll(createCommandMethod(command));
        }
        return methods;
    }

    /**
     * Create all methods for a command.
     * If necessary, deal with overloaded version (as well as we can ATM).
     * In case this is a {@code make} command, also generate a special Java version for it.
     *
     * @param command XML element
     * @return methods
     */
    private List<MethodSignature> createCommandMethod(final Element command) {

        final List<MethodSignature> methods = new ArrayList<>();

        final String name = command.getAttribute("name");
        final String description = command.getAttribute("description");
        final int overloadedVersions = getOverloadCount(command);

        // handle overloading, i.e. multiple sigs for different parameter types
        // for the moment, we only handle command with at most *one* overloaded parameter type
        for (int overloadCount = 0; overloadCount<overloadedVersions; overloadCount++) {

            final List<ParameterSignature> parameterSignatures = new ArrayList<>();
            final Set<String> alreadyUsedJavaParameterNames = new HashSet<>();
            String returnType = "void";
            String returnTypeDescription = null;

            final NodeList children = command.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                final Node child = children.item(i);
                if (child instanceof Element) {
                    final Element element = (Element) child;
                    if ("documentation".equals(element.getTagName())
                        || "xref".equals(element.getTagName())
                        || "access-group".equals(element.getTagName())
                        || "cocoa".equals(element.getTagName())
                        || "synonym".equals(element.getTagName())) {
                        // skip uninteresting elements
                        continue;
                    }

                    final String parameterType;
                    final boolean array;

                    if (element.getTagName().equals("direct-parameter")) {
                        // for the moment we support overloading only for the direct-parameter
                        parameterType = getParameterBaseType(element, overloadCount);
                        array = isParameterArray(element, overloadCount);
                    } else {
                        parameterType = getParameterBaseType(element, 0);
                        array = isParameterArray(element, 0);
                    }

                    final String parameterDescription = element.getAttribute("description");
                    final String javaParameterName = getParameterName(alreadyUsedJavaParameterNames,
                        parameterDescription, parameterType, array);

                    alreadyUsedJavaParameterNames.add(javaParameterName);

                    switch (element.getTagName()) {
                        case "direct-parameter": {
                            final ParameterSignature param = new ParameterSignature(javaParameterName,
                                parameterDescription,
                                getJavaType(parameterType, array));
                            parameterSignatures.add(param);
                            break;
                        }
                        case "parameter": {
                            final String parameterName = element.getAttribute("name");
                            final AnnotationSignature annotationSignature = new AnnotationSignature(Parameter.class,
                                "\"" + parameterName + "\"");
                            final ParameterSignature param = new ParameterSignature(javaParameterName,
                                parameterDescription,
                                getJavaType(parameterType, array),
                                annotationSignature);
                            parameterSignatures.add(param);
                            break;
                        }
                        case "result":
                            returnTypeDescription = parameterDescription;
                            returnType = getJavaType(parameterType, array);
                            break;
                    }
                }
            }

            final String methodName = Identifiers.toCamelCaseMethodName(name);
            final MethodSignature commandSignature = new MethodSignature(methodName);
            commandSignature.setDescription(toJavadocDescription(description));
            commandSignature.setReturnType(returnType);
            commandSignature.setReturnTypeDescription(returnTypeDescription);
            for (final ParameterSignature param : parameterSignatures) {
                commandSignature.add(param);
            }
            commandSignature.add(new AnnotationSignature(Kind.class, "\"command\""));
            commandSignature.add(new AnnotationSignature(Name.class, "\"" + name + "\""));

            methods.add(commandSignature);
        }

        if ("make".equals(name)) {

            final MethodSignature specialMake = new MethodSignature("make");
            specialMake.setDescription(toJavadocDescription(description));
            specialMake.setReturnType("<T extends " + Reference.class.getName() + "> T");
            specialMake.setReturnTypeDescription("a new object of type klass");
            specialMake.add(new ParameterSignature("klass", "Java type of the object to create.", "java.lang.Class<T>"));
            specialMake.add(new AnnotationSignature(com.tagtraum.japlscript.Kind.class, "\"make\""));

            methods.add(specialMake);
        }
        return methods;
    }

    private String getParameterBaseType(final Element element, final int overloadCount) {
        String parameterType = element.getAttribute("type");
        if (parameterType == null || parameterType.isEmpty()) {
            final NodeList types = element.getElementsByTagName("type");
            final List<Element> withTypes = getElementsWithNonEmptyAttribute(types, "type");
            final Element typeElement = withTypes.get(overloadCount);
            parameterType = typeElement.getAttribute("type");
        }
        return parameterType;
    }

    private boolean isParameterArray(final Element element, final int overloadCount) {
        boolean array = false;
        String parameterType = element.getAttribute("type");
        if (parameterType == null || parameterType.isEmpty()) {
            final NodeList types = element.getElementsByTagName("type");
            // this is not quite right. we just assume that once
            // we encounter a list with several types, we don't encounter
            // anything else anymore.
            final Element firstTypeChild = (Element) types.item(0);
            if (firstTypeChild.getAttribute("type").isEmpty() && "yes".equals(firstTypeChild.getAttribute("list"))) {
                return true;
            }
            final List<Element> withTypes = getElementsWithNonEmptyAttribute(types, "type");
            final Element typeElement = withTypes.get(overloadCount);
            array = "yes".equals(typeElement.getAttribute("list"));
        }
        return array;
    }

    private static List<Element> getElementsWithNonEmptyAttribute(final NodeList list, final String attributeName) {
        final List<Element> withAttribute = new ArrayList<>();
        for (int i = 0; i< list.getLength(); i++) {
            final Element item = (Element) list.item(i);
            if (!item.getAttribute(attributeName).isEmpty()) {
                withAttribute.add(item);
            }
        }
        return withAttribute;
    }

    /**
     * Return the number of overloaded versions of the given command
     * to generate.
     *
     * In the very specific case, that we have a direct-parameter
     * that allows multiple types, we map to overloaded Java methods.
     * In order to do so, we need to know how many overloaded versions
     * of the same command we need to generate.
     *
     * @param command command
     * @return number of overloaded versions, we need to generate
     */
    private int getOverloadCount(final Element command) {
        int overloadedVersions = 1;
        final NodeList c = command.getChildNodes();
        for (int j = 0; j < c.getLength(); j++) {
            final Node child = c.item(j);
            if (child instanceof Element) {
                final Element element = (Element) child;
                if ("direct-parameter".equals(element.getTagName())) {
                    final NodeList types = element.getElementsByTagName("type");
                    if (types.getLength() > 0) {
                        overloadedVersions = 0;
                        for (int i=0; i<types.getLength(); i++) {
                            if (!((Element)types.item(i)).getAttribute("type").isEmpty()) {
                                overloadedVersions++;
                            }
                        }
                        // overloadedVersions = types.getLength();
                    }
                }
            }
        }
        return overloadedVersions;
    }

    private static String getParameterName(final Collection<String> usedNames, final String description,
                                           final String type, final boolean isArray) {
        String newName;
        if (description != null && !description.isEmpty()) {
            final String newBaseName = Identifiers.toCamelCaseMethodName(description);
            newName = newBaseName;
            for (int i = 0; usedNames.contains(newName); i++) {
                newName = newBaseName + i;
            }
        } else {
            String newBaseName = Identifiers.toCamelCaseMethodName(type);
            if (isArray) newBaseName += "s";
            newName = newBaseName;
            for (int i = 0; usedNames.contains(newName); i++) {
                newName = newBaseName + i;
            }
        }
        return newName;
    }

    private List<MethodSignature> createElementMethods(final Element element) {
        final List<MethodSignature> methods = new ArrayList<>();
        final String type = element.getAttribute("type");
        final String javaClassName = getJavaType(type);
        final String propertyName = Identifiers.toCamelCaseClassName(type);
        final String access;
        if (isNullOrEmpty(element.getAttribute("access"))) access = "rw";
        else access = element.getAttribute("access");
        final String description = element.getAttribute("description");

        // this never really worked, which is why it has been disabled for now.
        // setter
        if (generateElementSetters && access.indexOf('w') != -1) {
            final MethodSignature setter = new MethodSignature("set" + propertyName);
            setter.setDescription(toJavadocDescription(description));
            setter.setReturnType("void");
            setter.setReturnTypeDescription(null);

            setter.add(new AnnotationSignature(Kind.class, "\"element\""));
            if (!isNullOrEmpty(type))
                setter.add(new AnnotationSignature(Type.class, "\"" + type + "\""));

            setter.add(new ParameterSignature("index", "index into the element list", "int"));
            setter.add(new ParameterSignature("value", "element to set in the list", javaClassName));
            methods.add(setter);
        }
        
        // getter and count
        if (access.indexOf('r') != -1) {

            final MethodSignature getterNoFilter = new MethodSignature("get" + propertyName + "s");
            getterNoFilter.setDescription(toJavadocDescription(description));
            getterNoFilter.setReturnType(javaClassName + "[]");
            getterNoFilter.setReturnTypeDescription("an array of all {@link " + javaClassName + "}s");
            getterNoFilter.add(new AnnotationSignature(Kind.class, "\"element\""));
            if (!isNullOrEmpty(type))
                getterNoFilter.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            getterNoFilter.setBody("return get" + propertyName + "s(null);");
            getterNoFilter.setDefaultMethod(true);
            methods.add(getterNoFilter);

            final MethodSignature getter = new MethodSignature("get" + propertyName + "s");
            getter.setDescription(toJavadocDescription(description));
            getter.setReturnType(javaClassName + "[]");
            getter.setReturnTypeDescription("an array of all {@link " + javaClassName + "}s");
            getter.add(new AnnotationSignature(Kind.class, "\"element\""));
            if (!isNullOrEmpty(type))
                getter.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            getter.add(new ParameterSignature("filter", "AppleScript filter clause without the leading \"whose\" or \"where\"", String.class.getName()));
            methods.add(getter);

            final MethodSignature getterWithIndex = new MethodSignature("get" + propertyName);
            getterWithIndex.setDescription(toJavadocDescription(description));
            getterWithIndex.setReturnType(javaClassName);
            getterWithIndex.setReturnTypeDescription("the {@link " + javaClassName + "} at the requested index");
            getterWithIndex.add(new AnnotationSignature(Kind.class, "\"element\""));
            if (!isNullOrEmpty(type))
                getterWithIndex.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            getterWithIndex.add(new ParameterSignature("index", "index into the element list", "int"));
            methods.add(getterWithIndex);

            final MethodSignature getterWithId = new MethodSignature("get" + propertyName);
            getterWithId.setDescription(toJavadocDescription(description));
            getterWithId.setReturnType(javaClassName);
            getterWithId.setReturnTypeDescription("the {@link " + javaClassName + "} with the requested id");
            getterWithId.add(new AnnotationSignature(Kind.class, "\"element\""));
            if (!isNullOrEmpty(type))
                getterWithId.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            getterWithId.add(new ParameterSignature("id", "id of the item", Id.class.getName()));
            methods.add(getterWithId);

            final MethodSignature countNoFilter = new MethodSignature("count" + propertyName + "s");
            countNoFilter.setDescription(toJavadocDescription(description));
            countNoFilter.setReturnType("int");
            countNoFilter.setReturnTypeDescription("number of all {@link " + javaClassName + "}s");
            countNoFilter.add(new AnnotationSignature(Kind.class, "\"element\""));
            if (!isNullOrEmpty(type))
                countNoFilter.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            countNoFilter.setBody("return count" + propertyName + "s(null);");
            countNoFilter.setDefaultMethod(true);
            methods.add(countNoFilter);

            final MethodSignature count = new MethodSignature("count" + propertyName + "s");
            count.setDescription(toJavadocDescription(description));
            count.setReturnType("int");
            count.setReturnTypeDescription("the number of elements that pass the filter");
            count.add(new AnnotationSignature(Kind.class, "\"element\""));
            if (!isNullOrEmpty(type))
                count.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            count.add(new ParameterSignature("filter", "AppleScript filter clause without the leading \"whose\" or \"where\"", String.class.getName()));
            methods.add(count);
        }
        return methods;
    }

    /**
     * Create {@link MethodSignature}s for a given property element.
     *
     * @param property XML element for a property
     * @param skipProperties skip generation for the property named "properties"
     * @return list of method signatures
     */
    private List<MethodSignature> createPropertyMethods(final Element property, final boolean skipProperties) {
        final List<MethodSignature> methods = new ArrayList<>();
        final String name = property.getAttribute("name");
        if (skipProperties && "properties".equals(name)) {
            return methods;
        }
        String type = property.getAttribute("type");
        boolean isArray = false;
        if (type == null || type.isEmpty()) {
            type = null;
            final NodeList types = property.getElementsByTagName("type");
            for (int i=0; i<types.getLength(); i++) {
                final Element typeElement = (Element) types.item(i);
                final String t = typeElement.getAttribute("type");
                if ("missing value".equals(t)) {
                    // ignore types "missing value"
                    continue;
                }
                if ("yes".equals(typeElement.getAttribute("hidden"))) {
                    // ignore hidden types
                    continue;
                }
                if (type != null) {
                    log("Cannot generate type-safe code for properties " +
                        "with multiple (non-null/missing value) types. Property: " + name, Level.WARNING);
                    type = "any";
                } else {
                    type = t;
                }
                isArray = "yes".equals(typeElement.getAttribute("list"));
            }
            if (types.getLength() == 1) {
                final Element typeElement = (Element) types.item(0);
                type = typeElement.getAttribute("type");
                isArray = "yes".equals(typeElement.getAttribute("list"));
            }
        }
        final String code = property.getAttribute("code");
        final String array;
        if (isArray) array = "[]";
        else array = "";
        final String javaClassName = getJavaType(type) + array;
        final String javaPropertyName = avoidForbiddenMethodNames(Identifiers.toCamelCaseClassName(name));
        final String access;
        if (isNullOrEmpty(property.getAttribute("access"))) access = "rw";
        else access = property.getAttribute("access");
        final String description = property.getAttribute("description");

        if (access.indexOf('r') != -1) {

            final MethodSignature getter = new MethodSignature("get" + javaPropertyName);
            getter.setDescription(toJavadocDescription(description));
            getter.setReturnType(javaClassName);
            getter.setReturnTypeDescription("Property value");

            getter.add(new AnnotationSignature(Kind.class, "\"property\""));
            if (!isNullOrEmpty(type))
                getter.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            if (!isNullOrEmpty(name))
                getter.add(new AnnotationSignature(Name.class, "\"" + name + "\""));
            if (!isNullOrEmpty(code))
                getter.add(new AnnotationSignature(Code.class, "\"" + code + "\""));
            methods.add(getter);
        }

        if (access.indexOf('w') != -1) {

            final MethodSignature setter = new MethodSignature("set" + javaPropertyName);
            setter.setDescription(toJavadocDescription(description));
            setter.setReturnType("void");
            setter.setReturnTypeDescription(null);
            
            setter.add(new AnnotationSignature(Kind.class, "\"property\""));
            if (!isNullOrEmpty(type))
                setter.add(new AnnotationSignature(Type.class, "\"" + type + "\""));
            if (!isNullOrEmpty(name))
                setter.add(new AnnotationSignature(Name.class, "\"" + name + "\""));
            if (!isNullOrEmpty(code))
                setter.add(new AnnotationSignature(Code.class, "\"" + code + "\""));
            
            setter.add(new ParameterSignature("object", "new property value", javaClassName));
            methods.add(setter);
        }
        return methods;
    }

    private String avoidForbiddenMethodNames(final String name) {
        if ("Class".equals(name)) return "Klass";
        return name;
    }

    private Path createClassFile(final String className) {
        return out.resolve(classToFile(className));
    }

    private String toPackageName(final String sdefName) {
        return packagePrefix + "." + sdefNameToPackageName(sdefName);
    }

    /**
     * Return the Java type for the given AppleScript type.
     *
     * @param applescriptType AppleScript type
     * @return Java type
     */
    private String getJavaType(final String applescriptType) {
        return getJavaType(applescriptType, false);
    }

    /**
     * Return the Java type for the given AppleScript type.
     *
     * @param applescriptType AppleScript type
     * @param array if true, return the array type
     * @return Java type (or type array)
     */
    private String getJavaType(final String applescriptType, final boolean array) {
        // do we have a custom mapping?
        String javaType = customTypeMapping.get(applescriptType);
        if (javaType == null) {
            // is the class defined in the in the current SDEF file?
            if (classMap.containsKey(applescriptType)) {
                javaType = Identifiers.toCamelCaseClassName(applescriptType);
            } else if (enumerationMap.containsKey(applescriptType)) {
                javaType = Identifiers.toCamelCaseClassName(applescriptType);
            }
        }
        if (javaType == null) {
            // do we have a standard mapping?
            javaType = JaplScript.getStandardJavaType(applescriptType);
        }
        if (javaType == null) {
            // fallback
            log("Unable to resolve AppleScript class '" + applescriptType
                    + "'. Will use plain Reference instead.", Level.WARNING);
            javaType = Reference.class.getName();
        }
        return array ? javaType + "[]" : javaType;
    }

    private static String sdefNameToPackageName(final String suiteName) {
        return suiteName.toLowerCase().replace(' ', '_');
    }

    private static String classToFile(final String packageName) {
        return packageName.replace('.', '/') + ".java";
    }

    private static String toJavadocDescription(final String s) {
        if (s == null || s.isEmpty()) return s;
        final StringBuilder sb = new StringBuilder(s.length() + 1);
        sb.append(Character.toUpperCase(s.charAt(0)));
        sb.append(s.substring(1));
        if (!s.endsWith(".") && !s.endsWith("?") && !s.endsWith("!")) sb.append('.');
        return sb.toString();
    }

    /**
     * Returns true, of the the given string is either null or empty.
     *
     * @param s string
     * @return true or false
     */
    private static boolean isNullOrEmpty(final String s) {
        return s == null || s.isEmpty();
    }

}
