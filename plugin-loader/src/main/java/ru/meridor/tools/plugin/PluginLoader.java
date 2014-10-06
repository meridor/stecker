package ru.meridor.tools.plugin;

import ru.meridor.tools.plugin.impl.*;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point class to plugin management. Simplest usage possible:
 *  <code>
 *      PluginRegistry plugins = PluginLoader
 *          .withInputFiles(...)
 *          .withExtensionPoints(Class1.class, Class2.class)
 *          .load();
 *  </code>
 *  You can also use your custom implementations of {@link FileFilter}, {@link ManifestReader} and so on like
 *  the following:
 *  <code>
 *      PluginRegistry plugins = PluginLoader
 *          .withInputFiles(...)
 *          .withExtensionPoints(Class1.class, Class2.class)
 *          .withManifestReader(new MyManifestReader())
 *          // ... and so on
 *          .load();
 *  </code>
 */
public class PluginLoader {

    private final FileProvider fileProvider;

    private List<Class> extensionPoints = new ArrayList<>();

    private FileFilter fileFilter;

    private ManifestReader manifestReader;

    private DependencyChecker dependencyChecker;

    private ClassLoader classLoader;

    private ClassesScanner classesScanner;

    /**
     * Specify a {@link FileProvider} which will return input files
     * @param fileProvider a list of input files
     * @return instance of plugin loader
     * @throws PluginException
     */
    public static PluginLoader withFileProvider(FileProvider fileProvider) throws PluginException {
        if (fileProvider == null){
            throw new PluginException("File provider can't be null");
        }
        return new PluginLoader(fileProvider);
    }

    /**
     * Define extension points to be considered by this loader
     * @param extensionPoints a list of extension point classes
     * @return this
     */
    public PluginLoader withExtensionPoints(Class...extensionPoints) {
        this.extensionPoints.addAll(
                Stream.of(extensionPoints)
                        .distinct()
                        .collect(Collectors.<Class>toList())
        );
        return this;
    }

    /**
     * Specify custom {@link FileFilter} implementation
     * @param fileFilter custom {@link FileFilter} implementation
     * @return this
     */
    public PluginLoader withFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
        return this;
    }

    /**
     * Specify custom {@link ManifestReader} implementation
     * @param manifestReader custom {@link ManifestReader} implementation
     * @return this
     */
    public PluginLoader withManifestReader(ManifestReader manifestReader) {
        this.manifestReader = manifestReader;
        return this;
    }

    /**
     * Specify custom {@link DependencyChecker} implementation
     * @param dependencyChecker custom {@link DependencyChecker} implementation
     * @return this
     */
    public PluginLoader withDependencyChecker(DependencyChecker dependencyChecker) {
        this.dependencyChecker = dependencyChecker;
        return this;
    }

    /**
     * Specify custom {@link ClassesScanner} implementation
     * @param classesScanner custom {@link ClassesScanner} implementation
     * @return this
     */
    public PluginLoader withClassesScanner(ClassesScanner classesScanner) {
        this.classesScanner = classesScanner;
        return this;
    }

    /**
     * Specify custom {@link ClassLoader}
     * @param classLoader custom {@link ClassLoader}
     * @return this
     */
    public PluginLoader withClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Returns currently used {@link FileProvider} instance
     * @return current file provider instance
     */
    public FileProvider getFileProvider() {
        return fileProvider;
    }

    /**
     * Returns currently used {@link FileFilter} instance
     * @return current file filter instance
     */
    public FileFilter getFileFilter() {
        return (fileFilter != null) ?
                fileFilter : new DefaultFileFilter();
    }

    /**
     * Returns current {@link ManifestReader} instance
     * @return current manifest reader instance
     */
    public ManifestReader getManifestReader() {
        return (manifestReader != null) ?
                manifestReader : new DefaultManifestReader();
    }

    /**
     * Returns current {@link DependencyChecker} instance
     * @return current dependency checker instance
     */
    public DependencyChecker getDependencyChecker() {
        return (dependencyChecker != null) ?
                dependencyChecker : new DefaultDependencyChecker();
    }

    /**
     * Returns current {@link ClassLoader} instance
     * @return current class loader instance
     */
    public ClassLoader getClassLoader() {
        return (classLoader != null) ?
                classLoader : getClass().getClassLoader();
    }

    /**
     * Returns current {@link ClassesScanner} instance
     * @return current classes scanner instance
     */
    public ClassesScanner getClassesScanner() {
        return (classesScanner != null) ?
                classesScanner : new DefaultClassesScanner(getClassLoader());
    }

    /**
     * Returns a list extension points classes
     * @return extension points list
     */
    public List<Class> getExtensionPoints() {
        return extensionPoints;
    }

    /**
     * Returns {@link PluginRegistry} storing information about loaded classes
     * @return plugin registry with loaded classes
     */
    public PluginRegistry load() throws PluginException {

        List<File> pluginFiles = getFileProvider().provide()
                .stream().filter(getFileFilter()::accept)
                .collect(Collectors.toList());

        PluginRegistry pluginRegistry = new PluginRegistryContainer();

        // Loading information about all plugins first
        for (File pluginFile: pluginFiles) {
            PluginMetadata pluginMetadata = getManifestReader().read(pluginFile);
            pluginRegistry.addPlugin(pluginMetadata);
        }

        // Iterating over the entire plugin set, checking for dependency resolution problems and loading classes
        for (String pluginName: pluginRegistry.getPluginNames()) {
            Optional<PluginMetadata> pluginMetadata = pluginRegistry.getPlugin(pluginName);
            if (pluginMetadata.isPresent()) {
                getDependencyChecker().check(pluginRegistry, pluginMetadata.get());

                Map<Class, List<Class>> mapping = getClassesScanner().scan(
                        getExtensionPoints(),
                        pluginMetadata.get().getFile()
                );
                for (Class extensionPoint: mapping.keySet()) {
                    pluginRegistry.addImplementations(extensionPoint, mapping.get(extensionPoint));
                }
            }
        }
        return pluginRegistry;
    }

    private PluginLoader(FileProvider fileProvider) throws PluginException {
        this.fileProvider = fileProvider;
    }

}
