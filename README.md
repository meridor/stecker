[![Build Status](https://travis-ci.org/meridor/stecker.svg?branch=master)](https://travis-ci.org/meridor/stecker)
# Stecker

Large applications very often provide plugin functionality. **Stecker** (which stands for "electrical plug" in Deutch) is a lightweight implementation of such plugin functionality that can be easily integrated to your project.

## Table of Contents
* [Getting Started](#getting-started)
  * [Example Project](https://github.com/meridor/stecker-example)
  * [Basic Usage](#basic-usage)
  * [Exceptions](#exceptions)
* [Creating Plugins](#creating-plugins)
* [Plugin Structure](#plugin-structure)
  * [Plugin Manifest Fields](#plugin-manifest-fields)
  * [Dependencies and Version Specification](#dependencies-and-version-specification)
  * [Extension Points](#extension-points)
* [Glossary](#glossary)
* [Internals](#internals)

## Getting Started
### Basic Usage
* Place plugins to a directory (e.g. `some/directory`)
* Add plugin loader to your Maven **pom.xml**:
```xml
<dependency>
    <groupId>org.meridor.stecker</groupId>
    <artifactId>stecker-plugin-loader</artifactId>
    <version>${latest-version}</version>
</dependency>
```
* Run the following code:
```java
Path aDirectoryWithPlugins = Paths.get("some/directory");
PluginRegistry pluginRegistry = PluginLoader
        .withPluginDirectory(aDirectoryWithPlugins)
        .withExtensionPoints(ExtensionPoint1.class, ExtensionPoint2.class, ExtensionPoint3.class)
        .withResourcesPatterns("glob:**/*.xml")
        .load();
```
This code will:
* Filter files matching specified glob (default is all \*.jar files) in the specified directory
* For each of matching files read their manifest and get plugin metadata
* Check plugin dependencies
* Unpack plugins to cache directory (default is **.cache** inside plugin directory) and scan it for extension points implementations
* Scan unpacked directory for resources matching specified pattern (\*.xml in the example above)
* Save all gathered information to container and return it

### Exceptions
The plugin engine always throws **PluginException**. When dependency problems occur you can determine what went wrong using the following code:
```java
    try {
        //Try to load plugins
    } catch (PluginException e) {
        Optional<DependencyProblem> problem = e.getDependencyProblem();
        if (problem.isPresent()) {
            List<Dependency> missingDependencies = problem.get().getMissingDependencies();
            List<Dependency> conflictingDependencies = problem.get().getConflictingDependencies();
            //Handle these lists somehow...
        }
    }
```

## Creating Plugins
To easily create a plugin you need to use Maven plugin called **plugin-generator**:
* Add the following to your **pom.xml**:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.meridor.stecker</groupId>
            <artifactId>stecker-plugin-generator</artifactId>
            <version>${latest-plugin-version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>create</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
* Run the build:
```bash
$ mvn clean package
```
* Find generated plugin jar file in **target/plugin-generator** directory.

## Plugin Structure
A plugin is simply a **[jar](http://en.wikipedia.org/wiki/JAR_%28file_format%29)** file containing:
* A [manifest](https://en.wikipedia.org/wiki/JAR_%28file_format%29#Manifest) with supplementary fields
* A **plugin.jar** file with compiled plugin classes
* Optionally a **lib/** folder with plugin dependencies, i.e. any third-party libraries used in plugin development

### Plugin Manifest Fields
Plugin manifest fields are highly influenced by ***.deb** package description [fields](https://www.debian.org/doc/debian-policy/ch-controlfields.html) and are prefixed with **Plugin-**.
* **Plugin-Name** - unique plugin name
* **Plugin-Version** - plugin version
* **Plugin-Date** - plugin release date
* **Plugin-Description** - human-readable plugin description
* **Plugin-Maintainer** - plugin maintainer name and email, e.g. `John Smith <john.smith@example.com>`
* **Plugin-Depends** - a list of plugins current plugin depends on
* **Plugin-Conflicts** - a list of plugins which conflict with current plugin
* **Plugin-Provides** - virtual plugin name which current plugin provides
The only required fields in manifest are **Plugin-Name** and **Plugin-Version**.

### Dependencies and Version Specification
This section mainly relates to such plugin manifest fields as **Plugin-Depends**, **Plugin-Conflicts** and **Plugin-Provides**:
* **Plugin-Depends** and **Plugin-Conflicts** should contain a list of dependency requirements expressed like the following:
```
Plugin-Depends:some-plugin=[1.0;);another-plugin=2.1.1;one-more-plugin=(,3.0)
```
This notation means that current plugin depends on 3 plugins: some-plugin with version greater than or equal to 1.0; another-plugin 2.1.1 and one-more-plugin with version lower than 3.0. As you can see plugin name is separated from its version by `=` (equality) sign and different dependency requirements are delimited by `;` (semicolon). 
* **Plugin-Provides** should contain only virtual plugin name:
```
Plugin-Provides:logger
```
Dependency version is following [Maven version specification rules](http://maven.apache.org/enforcer/enforcer-rules/versionRanges.html) and can be expressed in two ways:
* **Exact version**, e.g. `1.1`
* **Version range**, e.g. `[1.0,2.0)`
Version range is a pair of start and end versions enclosed in parentheses or square brackets or a pair of those. Required version should remain between start and end versions including them if square brackets are used and excluding them is case of parentheses. Some examples: `[1.0; 1.2]` - between 1.0 and 1.2 including them, `(,2.0)` - less than 2.0, `[2.2, 3.0)` - greater than or equals to 2.2 but less than 3.0. 

### Extension Points
An extension point can be an ordinary class (i.e. class or interface) or an annotation. In that case plugin loader returns classes that inherit from extension point class or are annotated by extension point annotation.

## Glossary
* **Host project** - a project supporting plugins.
* **Plugin** - a guest software project that adds a specific feature to host project.
* **Extension point** - a base class or an interface defined in the host project which is extended or implemented in a plugin. Examples: authentication strategy, security realm, GUI component, theme, data provider, validation strategy, persistence provider and so on. Host project should "know" how to deal with different extension points.
* **Dependency** - a pair of plugin name and version.
* **Virtual dependency** - unique name which in fact corresponds to a set of plugins providing the same functionality. For example, several plugins with different logger implementations can correspond to **logger** virtual dependency. Requiring a **logger** means requiring any of these implementations. There can be only one virtual dependency implementation present i.e. trying to load two plugins providing the same virtual dependency will result in error.
* **Dependency requirement** - a pair of plugin name and version range.
* **Version range** or **version requirement** - a pair of start and end version which is matched against plugin version.

## Internals
Internally plugin engine is based on the following interfaces:
* **PluginsProvider** - return a list of paths to be considered as plugin files or directories.
* **ManifestReader** - reads plugin manifest and returns an object with respective field values. Overriding default implementation can be used to change field names.
* **DependencyChecker** - uses data from manifest fields and checks that all required dependencies are present and no conflicting dependencies are present. To compare plugin versions an implementation of **VersionComparator** is used.
* **ClassesScanner** - scans **plugin.jar** file and searches for classes implementing extension points. Any class loading logic should be implemented here.
* **ResourcesScanner** - the same as **ClassesScanner** but for resource files.
All enumerated interfaces have default implementations but you can easily replace them with your own:
```java
File aDirectoryWithPlugins = new File("some/directory");
PluginRegistry pluginRegistry = PluginLoader
        .withPluginDirectory(aDirectoryWithPlugins)
        .withPluginsProvider(new MyCustomPluginsProvider())
        .withDependencyChecker(new MyCustomDependencyChecker())
        .withClassesScanner(new MyCustomClassesScanner())
        .withResourcesScanner(new MyCustomResourcesScanner())
        .load();
```
