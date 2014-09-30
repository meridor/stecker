# Plugin Engine
## Purpose
Large applications very often provide plugin functionality. This library is a simple implementation of such plugin functionality that can be easily integrated to your project.

## Glossary
* **Host project** - a project supporting plugins.
* **Plugin** - a guest software project that adds a specific feature to host project.
* **Extension point** - a base class or an interface defined in the host project which is extended or implemented in a plugin. Examples: authentication strategy, security realm, GUI component, theme, data provider, validation strategy, persistence provider and so on. Host project should "know" how to deal with different extension points.
* **Dependency** - a pair of plugin name and version.
* **Virtual dependency** - unique name which in fact corresponds to a set of plugins providing the same functionality. For example, several plugins with different logger implementations can correspond to **logger** virtual dependency. Requiring a **logger** means requiring any of these implementations. There can be only one virtual dependency implementation present i.e. trying to load two plugins providing the same virtual dependency will result in error.
* **Dependency requirement** - a pair of plugin name and version range.
* **Version range** or **version requirement** - a pair of start and end version which is matched against plugin version.

## Plugin Structure
A plugin is simply a **[jar](http://en.wikipedia.org/wiki/JAR_%28file_format%29)** file containing extension point implementations and a [manifest](https://en.wikipedia.org/wiki/JAR_%28file_format%29#Manifest) with supplementary fields.

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

## Extension Points
This library doesn't introduce any requirements on extension points. The only implicit extension point provided is the [Plugin](https://github.com/meridor/plugin-engine/blob/master/plugin-loader/src/main/java/ru/meridor/tools/plugin/Plugin.java) interface which allows you to determine plugin initialization and destruction logic (like [Servlet](http://docs.oracle.com/javaee/6/api/javax/servlet/Servlet.html) interface does).

## Basic Usage
The basic usage pattern is as follows.
1. Place plugins with all third-party libraries to a directory (e.g. `some/directory`)
2. Add plugin-loader to your Maven **pom.xml**:
```xml
    <dependency>
        <groupId>ru.meridor.tools</groupId>
        <artifactId>plugin-loader</artifactId>
        <version>${latest-version}</version>
    </dependency>
```
3. Run the following code:
```java
File aDirectoryWithPlugins = new File("some/directory");
PluginRegistry pluginRegistry = PluginLoader
        .withFileProvider(new DefaultFileProvider(aDirectoryWithPlugins))
        .withExtensionPoints(ExtensionPoint1.class, ExtensionPoint2.class, ExtensionPoint3.class)
        .load();
```
This code will:
 1. Scan all files in the specified directory
 2. Filter files matching `*-plugin.jar` pattern
 3. For each of matching files read their manifest and get plugin metadata
 4. Resolve plugin dependencies
 5. Scan matching files for extension point implementations
 6. Save all gathered information to container and return it

## Exceptions
In case of errors plugin engine throws an exception of the same type - **PluginException**. When dependency problems occur you can determine what went wrong using the following code:
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

## Internals
Internally plugin engine is based on the following interfaces:
* **FileProvider** - returns a list of files to be processed. Default implementation simply scans directory specified and returns all files.
* **FileFilter** - processes a list of provided files and returns which of them seem to be plugins. It's mainly about matching files name against some pattern, e.g. only leave **jar** files having **plugin** in name (default).
* **ManifestReader** - reads plugin manifest and returns an object with respective field values. Overriding default implementation can be used to change field names.
* **DependencyChecker** - uses data from manifest fields and checks that all required dependencies are present and no conflicting dependencies are present. To compare plugin versions an implementation of **VersionComparator** is used.
* **ClassesScanner** - scans **jar** files and search for classes implementing extension points.
 and the full plugin engine life cycle is:
1. Get a list of processed files from **FileProvider**
2. Filter out plugin files with **FileFilter**
3. Read plugin metadata with **ManifestReader**
4. Check plugin dependencies with **DependencyChecker**
5. Return a list of extension points using **ClassesScanner**

All enumerated interfaces have default implementations but you can easily replace them with your own:
```java
File aDirectoryWithPlugins = new File("some/directory");
PluginRegistry pluginRegistry = PluginLoader
        .withFileProvider(new DefaultFileProvider(aDirectoryWithPlugins))
        .withFileFilter(new MyFileFilter())
        .withClassesScanner(new MyCustomClassesScanner())
        .withExtensionPoints(ExtensionPoint.class)
        .load();
```

## Extras
### Maven File Provider
Default **FileProvider** interface implementation scans a single directory with plugins and returns all contained files. From Java perspective this is is a problem because:
1. You should store all plugins in the same folder
2. You should place libraries which are used in plugins to the folder with plugin jar (no automatic dependency management)
To resolve these problems you can use **MavenFileProvider** as follows. In pom.xml:
```xml
    <dependency>
        <groupId>ru.meridor.tools</groupId>
        <artifactId>maven-file-provider</artifactId>
        <version>${latest-version}</version>
    </dependency>
```
In your code:
```java
File mavenLocalArtifactRepository = new File("directory/to/store/Maven/artifact/repository");
PluginRegistry pluginRegistry = PluginLoader
        .withFileProvider(new MavenFileProvider(
            mavenLocalArtifactRepository,
            "groupId1:artifactId1:version1", "groupId2:artifactId2:version2"
        ))
        .withExtensionPoints(ExtensionPoint1.class, ExtensionPoint2.class)
        .load();
```
This class will automatically download all dependencies for plugin Maven artifacts **groupId1:artifactId1:version1** and **groupId2:artifactId2:version2** and return their paths to plugin engine.