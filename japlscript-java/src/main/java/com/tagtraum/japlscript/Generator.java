/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.TypeClass;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Generates Interface source files for an <code>.sdef</code> file.
 * Can be used as <a href="http://ant.apache.org/">Ant</a> task.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Generator extends Task {

    private static final String SDEF_DTD = "file://localhost/System/Library/DTDs/sdef.dtd";
    private Path sdef;
    private String packagePrefix = "com.tagtraum.japlscript";
    private Path out = Paths.get(".");
    private final Map<String, String> customTypeMapping = new HashMap<>();
    private final Set<String> excludeClassSet = new HashSet<>();
    private Map<String, List<Element>> classMap;
    private Map<String, List<Element>> enumerationMap;

    /**
     *
     * @param typeMapping type mapping
     */
    public void addConfiguredTypeMapping(final TypeMapping typeMapping) {
        customTypeMapping.put(typeMapping.getApplescript(), typeMapping.getJava());
    }

    /**
     *
     * @param excludeClass excluded class
     */
    public void addConfiguredExcludeClass(final ExcludeClass excludeClass) {
        excludeClassSet.add(excludeClass.getName());
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

    @Override
    public void execute() {
        try {
            generate();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * Generates JaplScript classes/interfaces.
     *
     * @throws ParserConfigurationException parser issues
     * @throws IOException IO issues
     * @throws SAXException XML parsing issues
     */
    public void generate() throws ParserConfigurationException, IOException, SAXException {
        log("Generating sources...", Project.MSG_INFO);
        log("Sdef: " + sdef, Project.MSG_INFO);
        log("Generation output path : " + out, Project.MSG_INFO);
        log("Package prefix: " + packagePrefix, Project.MSG_INFO);
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(final String publicId, final String systemId)  {
                if (SDEF_DTD.equals(systemId)) {
                    final InputStream sdefDTD = getClass().getResourceAsStream("sdef.dtd");
                    assert sdefDTD != null;
                    return new InputSource(sdefDTD);
                }
                return null;
            }
        });
        final Document sdefDocument = documentBuilder.parse(this.sdef.toFile());

        buildClassMap(sdefDocument);

        buildEnumerationMap(sdefDocument);

        for (Map.Entry<String, List<Element>> entry : classMap.entrySet()) {
            if (!excludeClassSet.contains(entry.getKey())) {
                writeClass(entry.getValue());
            }
        }

        final NodeList enumerations = sdefDocument.getElementsByTagName("enumeration");
        final int enumerationsLength = enumerations.getLength();
        for (int j = 0; j < enumerationsLength; j++) {
            final Element enumeration = (Element) enumerations.item(j);
            writeEnumeration(enumeration);
        }
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
            List<Element> list = classMap.computeIfAbsent(valueTypeName, k -> new ArrayList<>());
            list.add(valueType);
        }

        if (classMap.isEmpty()) {
            log("SDEF does not contain any classes. Adding artificial Application class.", Project.MSG_WARN);
        }
    }

    private void writeEnumeration(final Element enumeration)
            throws IOException {
        final String className = enumeration.getAttribute("name");
        final String fullyQualifiedClassName = toFullyQualifiedClassName(className);
        final String javaClassName = getJavaType(className);
        final Path classFile = createClassFile(fullyQualifiedClassName);
        Files.createDirectories(classFile.getParent());
        try (final BufferedWriter streamWriter = Files.newBufferedWriter(classFile, StandardCharsets.UTF_8)) {
            final PrintWriter writer = new PrintWriter(streamWriter);
            final String packageName = getPackageName();
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("/**");
            writer.println(" * " + enumeration.getAttribute("description"));
            writer.println(" */");
            if (enumeration.getAttribute("code") != "")
                writer.println("@" + com.tagtraum.japlscript.Code.class.getName()
                        + "(\"" + enumeration.getAttribute("code") + "\")");
            if (className != "")
                writer.println("@" + com.tagtraum.japlscript.Name.class.getName() + "(\"" + className + "\")");
            writer.println("public enum " + javaClassName + " implements " + JaplEnum.class.getName() + " {");
            writer.println();
            // enumerators
            final NodeList enumerators = enumeration.getElementsByTagName("enumerator");
            for (int i = 0; i < enumerators.getLength(); i++) {
                final Element enumerator = (Element) enumerators.item(i);
                writeEnumerator(writer, enumerator);
                if (i + 1 < enumerators.getLength()) writer.println(",");
                else writer.println(";");
            }
            writer.println();
            writer.println("private final String name;");
            writer.println("private final String code;");
            writer.println("private final String description;");
            writer.println();
            writer.println("private " + javaClassName + "(final String name, final String code, final String description) {");
            writer.println("    this.name = name;");
            writer.println("    this.code = code;");
            writer.println("    this.description = description;");
            writer.println("}");
            writer.println();
            writer.println("@Override");
            writer.println("public String getName() { return this.name;}");
            writer.println();
            writer.println("@Override");
            writer.println("public String getCode() { return this.code;}");
            writer.println();
            writer.println("@Override");
            writer.println("public String getDescription() { return this.description;}");
            writer.println();
            writer.println("/**");
            writer.println(" * Get instance for name.");
            writer.println(" */");
            writer.println("public static " + javaClassName + " get(final String name) {");
            // get(name) method
            for (int i = 0; i < enumerators.getLength(); i++) {
                final Element enumerator = (Element) enumerators.item(i);
                final String name = enumerator.getAttribute("name");
                final String code = enumerator.getAttribute("code");
                final String javaName = Types.toJavaConstant(name);
                if (i != 0) writer.print("    else ");
                else writer.print("    ");
                writer.println("if (\"" + code + "\".equals(name) || \"" + name + "\".equals(name) || \"\u00abconstant ****"
                        + code + "\u00bb\".equals(name)) return " + javaName + ";");
            }
            writer.println("    else throw new " + IllegalArgumentException.class.getName()
                    + "(\"Enum \" + name + \" is unknown.\");");
            writer.println("}");
            writer.println();
            writer.println("}");
            writer.flush();
        }
    }

    private void writeEnumerator(final PrintWriter writer, final Element enumerator) {
        final String name = enumerator.getAttribute("name");
        final String code = enumerator.getAttribute("code");
        final String description;
        if (enumerator.getAttribute("description") == "") description = "null";
        else description = "\"" + enumerator.getAttribute("description") + "\"";
        final String javaName = Types.toJavaConstant(name);
        writer.print("    " + javaName + "(\"" + name + "\", \"" + code + "\", " + description + ")");
    }

    private void writeClass(final List<Element> classList) throws IOException {
        final Element klass = classList.get(0);
        final String className = klass.getAttribute("name") != null &&klass.getAttribute("name").length() > 0 ? klass.getAttribute("name") : klass.getAttribute("extends");
        final String fullyQualifiedClassName = toFullyQualifiedClassName(className);
        final String javaClassName = Types.toCamelCaseClassName(className);
        final Path classFile = createClassFile(fullyQualifiedClassName);
        Files.createDirectories(classFile.getParent());
        final Set<MethodSignature> methodSignatures = new HashSet<>();
        try (final BufferedWriter streamWriter = Files.newBufferedWriter(classFile, StandardCharsets.UTF_8)) {
            final PrintWriter writer = new PrintWriter(streamWriter);
            final String packageName = getPackageName();
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(klass.getAttribute("description")));
            writer.println(" */");
            final String superClass = klass.getAttribute("inherits");
            String code = "null";
            String typeSuperClass = "null";
            if (klass.getAttribute("plural") != "")
                writer.println("@" + com.tagtraum.japlscript.Plural.class.getName()
                        + "(\"" + klass.getAttribute("plural") + "\")");
            if (klass.getAttribute("code") != "")
                code = "\"\\u00abclass " + klass.getAttribute("code") + "\\u00bb\"";
                writer.println("@" + com.tagtraum.japlscript.Code.class.getName()
                        + "(\"" + klass.getAttribute("code") + "\")");
            if (className != "")
                writer.println("@" + com.tagtraum.japlscript.Name.class.getName() + "(\"" + className + "\")");
            if (superClass != "") {
                writer.println("@" + com.tagtraum.japlscript.Inherits.class.getName() + "(\"" + superClass + "\")");
            }
            writer.print("public interface " + javaClassName + " extends " + Reference.class.getName());
            if (superClass != "" && !superClass.equals(className)) {
                // TODO: package name for super class....
                final String additionalSuperClass = getJavaType(superClass);
                if (!Reference.class.getName().equals(additionalSuperClass)) {
                    writer.print(", " + additionalSuperClass);
                    typeSuperClass = additionalSuperClass + ".CLASS";
                }
            }
            writer.println(" {");
            writer.println();
            writer.println("static final " + TypeClass.class.getName()
                    + " CLASS = " + TypeClass.class.getName() + ".getInstance(\"" + className + "\", " + code + ", null, " + typeSuperClass + ");");

            // check for application class
            if ("application".equals(className)) {
                // commands
                writeCommands(writer, klass.getOwnerDocument(), methodSignatures);
            }

            for (Element classElement : classList) {
                // elements
                final NodeList elements = classElement.getElementsByTagName("element");
                for (int i = 0; i < elements.getLength(); i++) {
                    final Element element = (Element) elements.item(i);
                    writeElement(writer, element, methodSignatures);
                }
                // properties
                final NodeList properties = classElement.getElementsByTagName("property");
                for (int i = 0; i < properties.getLength(); i++) {
                    final Element property = (Element) properties.item(i);
                    writeProperty(writer, property, methodSignatures);
                }
            }
            writer.println();
            writer.println("}");
            writer.flush();
        }
    }

    private void writeCommands(final PrintWriter writer, final Document document,
                               final Set<MethodSignature> methodSignatures) {
        // commands
        final NodeList commands = document.getElementsByTagName("command");
        for (int i = 0; i < commands.getLength(); i++) {
            final Element command = (Element) commands.item(i);
            writeCommand(writer, command, methodSignatures);
        }
        // write cast
    }

    private void writeCommand(final PrintWriter w, final Element command, final Set<MethodSignature> methodSignatures) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        final String name = command.getAttribute("name");
        final String description = command.getAttribute("description");
        writer.println();
        writer.println("/**");
        writer.println(" * " + toJavadocDescription(description));
        writer.println(" *");
        // direct param, if it exists
        final NodeList children = command.getChildNodes();
        final List<String> javaParameterNames = new ArrayList<>();
        final List<String> parameterNames = new ArrayList<>();
        final List<String> parameterTypes = new ArrayList<>();
        final List<Boolean> parameterArray = new ArrayList<>();
        boolean hasResult = false;
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child instanceof Element) {
                boolean isArray = false;
                final Element element = (Element) child;
                if ("access-group".equals(element.getTagName()) || "cocoa".equals(element.getTagName()) || "synonym".equals(element.getTagName())) {
                    // skip certain elements
                    continue;
                }
                String parameterType = element.getAttribute("type");
                final String parameterDescription = element.getAttribute("description");
                if (parameterType == null || parameterType.length() == 0) {
                    final NodeList types = element.getElementsByTagName("type");
                    if (types.getLength() > 1) {
                        log("Cannot generate code for commands with multiple types. Command: " + name);
                        log("Will skip further commands with other parameters.");
                    }
                    if (types.getLength() >= 1) {
                        final Element typeElement = (Element) types.item(0);
                        parameterType = typeElement.getAttribute("type");
                        isArray = "yes".equals(typeElement.getAttribute("list"));
                    }
                }
                final String javaParameterName = getParameterName(javaParameterNames,
                        parameterDescription, parameterType, isArray);
                javaParameterNames.add(javaParameterName);
                parameterTypes.add(parameterType);
                parameterArray.add(isArray);
                if ("direct-parameter".equals(element.getTagName())) {
                    writer.println(" * @param " + javaParameterName + " " + parameterDescription);
                    parameterNames.add("");
                } else if ("parameter".equals(element.getTagName())) {
                    writer.println(" * @param " + javaParameterName + " " + parameterDescription);
                    parameterNames.add(element.getAttribute("name"));
                } else if ("result".equals(element.getTagName())) {
                    writer.println(" * @return " + parameterDescription);
                    hasResult = true;
                }
            }
        }
        final int parameterCount;
        if (hasResult) parameterCount = parameterTypes.size() - 1;
        else parameterCount = parameterTypes.size();
        writer.println(" */");
        writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"command\")");
        writer.println("@" + com.tagtraum.japlscript.Name.class.getName() + "(\"" + name + "\")");
        final MethodSignature methodSignature = new MethodSignature();
        // writer.print("public "); // interfaces are always public
        if (hasResult) {
            String returnType = getJavaType(parameterTypes.get(parameterTypes.size() - 1));
            if (parameterArray.get(parameterArray.size() - 1)) returnType += "[]";
            writer.print(returnType);
            writer.print(" ");
            methodSignature.setReturnType(returnType);
        } else {
            writer.print("void ");
            methodSignature.setReturnType("void");
        }
        final String methodName = Types.toCamelCaseMethodName(name);
        methodSignature.setName(methodName);
        writer.print(methodName + "(");
        for (int i = 0; i < parameterCount; i++) {
            if (!parameterNames.get(i).isEmpty()) {
                writer.print("@" + Parameter.class.getName() + "(\"" + parameterNames.get(i) + "\")");
                writer.print(" ");
            }
            String parameterType = getJavaType(parameterTypes.get(i));
            if (parameterArray.get(i)) parameterType += "[]";
            writer.print(parameterType);
            writer.print(" ");
            writer.print(javaParameterNames.get(i));
            if (i + 1 < parameterCount) writer.print(", ");
            methodSignature.addParameterType(parameterType);
        }
        writer.println(");");
        if ("make".equals(name)) {
            // write special make methods
            writer.println();
            writer.println("/**");
            writer.println(" * Creates a new object.");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" *");
            writer.println(" * @param klass Java type of the object to create.");
            writer.println(" * @return a new object of type klass");
            writer.println(" */");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"make\")");
            writer.println("public <T extends " + Reference.class.getName() + "> T make(Class<T> klass);");
            /*
            writer.println("public " + Reference.class.getName() + " make(Class klass, "
                    + LocationReference.class.getName() + " whereToInsert);");
            writer.println("public " + Reference.class.getName() + " make(Class klass, "
                    + LocationReference.class.getName() + " whereToInsert, "
                    + Record.class.getName() + " initialPropertyValues);");
            */
        }
        writer.flush();
        if (!methodSignatures.contains(methodSignature)) {
            w.write(stringWriter.toString());
            methodSignatures.add(methodSignature);
        } else {
            log("Skipping method " + methodSignature + " since it is already declared.");
        }
    }

    private static String getParameterName(final List<String> usedNames, final String description,
                                           final String type, final boolean isArray) {
        String newName;
        if (description != null && description.length() > 0) {
            final String newBaseName = Types.toCamelCaseMethodName(description);
            newName = newBaseName;
            for (int i = 0; usedNames.contains(newName); i++) {
                newName = newBaseName + i;
            }
        } else {
            String newBaseName = Types.toCamelCaseMethodName(type);
            if (isArray) newBaseName += "s";
            newName = newBaseName;
            for (int i = 0; usedNames.contains(newName); i++) {
                newName = newBaseName + i;
            }
        }
        return newName;
    }

    private void writeElement(final PrintWriter w, final Element element, final Set<MethodSignature> methodSignatures) {
        final String type = element.getAttribute("type");
        final String javaClassName = getJavaType(type);
        final String propertyName = Types.toCamelCaseClassName(type);
        final String access;
        if (element.getAttribute("access") == "") access = "rw";
        else access = element.getAttribute("access");
        final String description = element.getAttribute("description");

        // setter
        if (access.indexOf('w') != -1) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter writer = new PrintWriter(stringWriter);

            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" * @param value element to set in the list");
            writer.println(" * @param index index into the element list");
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"element\")");
            writer.println("void set" + propertyName + "(" + javaClassName + " value, int index);");

            final MethodSignature methodSignature = new MethodSignature();
            methodSignature.setReturnType("void");
            methodSignature.setName("set" + propertyName);
            methodSignature.addParameterType(javaClassName);
            methodSignature.addParameterType("int");
            if (!methodSignatures.contains(methodSignature)) {
                w.write(stringWriter.toString());
                methodSignatures.add(methodSignature);
            } else {
                log("Skipping element " + type + " since it is already declared.");
            }
        }
        
        // getter and count
        if (access.indexOf('r') != -1) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter writer = new PrintWriter(stringWriter);

            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" * @return an array of all {@link " + javaClassName + "}s");
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"element\")");
            writer.println("default " + javaClassName + "[] get" + propertyName + "s() {");
            writer.println("    return get" + propertyName + "s(null);");
            writer.println("}");

            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" * @param filter AppleScript filter clause without the leading \"whose\" or \"where\"");
            writer.println(" * @return a filtered array of {@link " + javaClassName + "}s");
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"element\")");
            writer.println(javaClassName + "[] get" + propertyName + "s(String filter);");

            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" * @param index index into the element list");
            writer.println(" * @return the {@link " + javaClassName + "} with at the requested index");
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"element\")");
            writer.println(javaClassName + " get" + propertyName + "(int index);");

            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" * @param id id of the item");
            writer.println(" * @return the {@link " + javaClassName + "} with the requested id");
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"element\")");
            writer.println(javaClassName + " get" + propertyName + "(" + Id.class.getName() + " id);");

            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" * @return number of all {@link " + javaClassName + "}s");
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"element\")");
            writer.println("default int count" + propertyName + "s() {");
            writer.println("    return count" + propertyName + "s(null);");
            writer.println("}");

            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" * @param filter AppleScript filter clause without the leading \"whose\" or \"where\"");
            writer.println(" * @return the number of elements that pass the filter");
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"element\")");
            writer.println("int count" + propertyName + "s(String filter);");

            MethodSignature methodSignature = new MethodSignature();
            methodSignature.setReturnType(javaClassName + "[]");
            methodSignature.setName("get" + propertyName);
            if (!methodSignatures.contains(methodSignature)) {
                w.write(stringWriter.toString());
                methodSignatures.add(methodSignature);
            } else {
                log("Skipping element " + type + " since it is already declared.");
            }
        }
    }

    private void writeProperty(final PrintWriter w, final Element property,
                               final Set<MethodSignature> methodSignatures) {
        final String name = property.getAttribute("name");
        String type = property.getAttribute("type");
        boolean isArray = false;
        if (type == null || type.length() == 0) {
            final NodeList types = property.getElementsByTagName("type");
            if (types.getLength() > 1)
                throw new RuntimeException("Cannot generate code for properties with multiple types. Property: " + name);
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
        final String javaPropertyName = avoidForbiddenMethodNames(Types.toCamelCaseClassName(name));
        final String access;
        if (property.getAttribute("access") == "") access = "rw";
        else access = property.getAttribute("access");
        final String description = property.getAttribute("description");
        if (access.indexOf('r') != -1) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter writer = new PrintWriter(stringWriter);
            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            if (name != "") writer.println("@" + com.tagtraum.japlscript.Name.class.getName() + "(\"" + name + "\")");
            if (code != "") writer.println("@" + com.tagtraum.japlscript.Code.class.getName() + "(\"" + code + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"property\")");
            writer.println(javaClassName + " get" + javaPropertyName + "();");
            writer.flush();
            final MethodSignature methodSignature = new MethodSignature();
            methodSignature.setReturnType(javaClassName);
            methodSignature.setName("get" + javaPropertyName);
            if (!methodSignatures.contains(methodSignature)) {
                w.write(stringWriter.toString());
                methodSignatures.add(methodSignature);
            } else {
                log("Skipping method " + methodSignature + " since it is already declared.");
            }
        }
        if (access.indexOf('w') != -1) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter writer = new PrintWriter(stringWriter);
            writer.println();
            writer.println("/**");
            writer.println(" * " + toJavadocDescription(description));
            writer.println(" */");
            if (type != "") writer.println("@" + com.tagtraum.japlscript.Type.class.getName() + "(\"" + type + "\")");
            if (name != "") writer.println("@" + com.tagtraum.japlscript.Name.class.getName() + "(\"" + name + "\")");
            if (code != "") writer.println("@" + com.tagtraum.japlscript.Code.class.getName() + "(\"" + code + "\")");
            writer.println("@" + com.tagtraum.japlscript.Kind.class.getName() + "(\"property\")");
            writer.println("void set" + javaPropertyName + "(" + javaClassName + " object);");
            writer.flush();
            MethodSignature methodSignature = new MethodSignature();
            methodSignature.setReturnType("void");
            methodSignature.setName("set" + javaPropertyName);
            methodSignature.addParameterType(javaClassName);
            if (!methodSignatures.contains(methodSignature)) {
                w.write(stringWriter.toString());
                methodSignatures.add(methodSignature);
            } else {
                log("Skipping method " + methodSignature + " since it is already declared.");
            }
        }
    }

    private String avoidForbiddenMethodNames(final String name) {
        if ("Class".equals(name)) return "Klass";
        return name;
    }

    private Path createClassFile(final String className) {
        return out.resolve(classToFile(className));
    }

    private String toFullyQualifiedClassName(final String className) {
        return getPackageName() + "." + Types.toCamelCaseClassName(className);
    }

    private String toPackageName(final String sdefName) {
        return packagePrefix + "." + sdefNameToPackageName(sdefName);
    }

    private String getJavaType(final String applescriptType) {
        // do we have a custom mapping?
        String javaType = customTypeMapping.get(applescriptType);
        if (javaType == null) {
            // is the class defined in the in the current SDEF file?
            if (classMap.containsKey(applescriptType)) {
                javaType = Types.toCamelCaseClassName(applescriptType);
            } else if (enumerationMap.containsKey(applescriptType)) {
                javaType = Types.toCamelCaseClassName(applescriptType);
            }
        }
        if (javaType == null) {
            // do we have a standard mapping?
            javaType = Types.getStandardJavaType(applescriptType);
        }
        if (javaType == null) {
            // fallback
            log("Warning: Unable to resolve Applescript class '" + applescriptType
                    + "'. Will use plain Reference instead.");
            javaType = Reference.class.getName();
        }
        return javaType;
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

    /*
    public void log(String s) {
        System.out.println(s);
    }
    */

    /**
     *
     * @param args args
     * @throws ParserConfigurationException parser issues
     * @throws IOException IO issues
     * @throws SAXException XML parsing issues
     */
    public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
        final Generator generator = new Generator();
        generator.setSdef(new java.io.File(args[0]).toPath());
        generator.setOut(Paths.get("out"));
        generator.generate();
    }

    /**
     * Type mapping.
     */
    public static class TypeMapping {
        private String applescript;
        private String java;

        public String getApplescript() {
            return applescript;
        }

        public void setApplescript(final String applescript) {
            this.applescript = applescript;
        }

        public String getJava() {
            return java;
        }

        public void setJava(final String java) {
            this.java = java;
        }
    }

    /**
     * Exclude class.
     */
    public static class ExcludeClass {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    private static class MethodSignature {
        private String returnType = "";
        private String name = "";
        private final List<String> parameterTypes = new ArrayList<>();

        public MethodSignature() {
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(final String returnType) {
            this.returnType = returnType;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void addParameterType(final String type) {
            this.parameterTypes.add(type);
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof MethodSignature)) return false;
            final MethodSignature that = (MethodSignature) obj;
            return this.name.equals(that.name)
                    && this.returnType.equals(that.returnType)
                    && this.parameterTypes.equals(that.parameterTypes);
        }

        @Override
        public String toString() {
            return returnType + " " + name + " " + parameterTypes;
        }
    }
}
