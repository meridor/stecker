package org.meridor.stecker;

import org.meridor.stecker.impl.DefaultClassesScanner;
import org.meridor.stecker.impl.DefaultDependencyChecker;
import org.meridor.stecker.impl.DefaultManifestReader;
import org.meridor.stecker.impl.DefaultPluginsProvider;
import org.meridor.stecker.impl.DefaultResourcesScanner;
import org.meridor.stecker.impl.PluginRegistryContainer;
import org.meridor.stecker.interfaces.ClassesScanner;
import org.meridor.stecker.interfaces.DependencyChecker;
import org.meridor.stecker.interfaces.ManifestReader;
import org.meridor.stecker.interfaces.PluginsProvider;
import org.meridor.stecker.interfaces.ResourcesScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point class to plugin management
 */
public class PluginLoader {

    private static final String DEFAULT_FILE_GLOB = "**/*.jar";
    private String fileGlob = DEFAULT_FILE_GLOB;
    public static final String DEFAULT_CACHE_DIRECTORY = ".cache";
    private final Path pluginsDirectory;
    private final List<Class> extensionPoints = new ArrayList<>();
    private Path cacheDirectory;

    private PluginsProvider pluginsProvider;

    private ManifestReader manifestReader;

    private DependencyChecker dependencyChecker;

    private ClassesScanner classesScanner;

    private ResourcesScanner resourcesScanner;

    private String[] resourcesPatterns = new String[0];

    private PluginLoader(Path pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;
    }

    /**
     * Define the directory to search for plugins
     *
     * @param pluginsDirectory directory containing plugins
     * @return this
     * @throws PluginException
     */
    public static PluginLoader withPluginDirectory(Path pluginsDirectory) throws PluginException {
        if (pluginsDirectory == null) {
            throw new PluginException("Plugins directory can't be null");
        }
        return new PluginLoader(pluginsDirectory);
    }

    /**
     * Define file glob to be used when searching for plugins
     *
     * @param fileGlob file search glob
     * @return this
     */
    public PluginLoader withFileGlob(String fileGlob) {
        this.fileGlob = fileGlob;
        return this;
    }

    /**
     * Define plugins provider instance to be used
     *
     * @param pluginsProvider plugins provider instance
     * @return this
     */
    public PluginLoader withPluginsProvider(PluginsProvider pluginsProvider) {
        this.pluginsProvider = pluginsProvider;
        return this;
    }

    /**
     * Define cache directory used to extract plugin files
     *
     * @param cacheDirectory path to cache directory
     * @return this
     */
    public PluginLoader withCacheDirectory(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        return this;
    }

    /**
     * Define extension points to be considered by this loader
     *
     * @param extensionPoints a list of extension point classes
     * @return this
     */
    public PluginLoader withExtensionPoints(Class... extensionPoints) {
        this.extensionPoints.addAll(
                Stream.of(extensionPoints)
                        .distinct()
                        .collect(Collectors.<Class>toList())
        );
        return this;
    }

    /**
     * Specify custom {@link ManifestReader} implementation
     *
     * @param manifestReader custom {@link ManifestReader} implementation
     * @return this
     */
    public PluginLoader withManifestReader(ManifestReader manifestReader) {
        this.manifestReader = manifestReader;
        return this;
    }

    /**
     * Specify custom {@link DependencyChecker} implementation
     *
     * @param dependencyChecker custom {@link DependencyChecker} implementation
     * @return this
     */
    public PluginLoader withDependencyChecker(DependencyChecker dependencyChecker) {
        this.dependencyChecker = dependencyChecker;
        return this;
    }

    /**
     * Specify custom {@link ClassesScanner} implementation
     *
     * @param classesScanner custom {@link ClassesScanner} implementation
     * @return this
     */
    public PluginLoader withClassesScanner(ClassesScanner classesScanner) {
        this.classesScanner = classesScanner;
        return this;
    }

    /**
     * Specify custom {@link ResourcesScanner} implementation
     *
     * @param resourcesScanner custom {@link ResourcesScanner} implementation
     * @return this
     */
    public PluginLoader withResourcesScanner(ResourcesScanner resourcesScanner) {
        this.resourcesScanner = resourcesScanner;
        return this;
    }

    /**
     * Specify resources patterns to match against
     *
     * @param resourcesPatterns a list of allowed resources patterns in {@link java.nio.file.PathMatcher} format
     * @return this
     * @see java.nio.file.FileSystem#getPathMatcher
     */
    public PluginLoader withResourcesPatterns(String... resourcesPatterns) {
        this.resourcesPatterns = resourcesPatterns;
        return this;
    }

    /**
     * Returns the directory where we search for plugins
     *
     * @return plugin directory
     */
    public Path getPluginsDirectory() {
        return pluginsDirectory;
    }

    /**
     * Returns the glob used when looking for plugin files
     *
     * @return file glob
     */
    public String getFileGlob() {
        return fileGlob;
    }

    /**
     * Returns plugins provider instance used during loader run
     *
     * @return plugins provider instance
     */
    public PluginsProvider getPluginsProvider() {
        return (pluginsProvider != null) ? pluginsProvider : new DefaultPluginsProvider(getFileGlob());
    }

    /**
     * Returns the cache directory to which we unpack plugin contents
     *
     * @return cache directory
     */
    public Path getCacheDirectory() {
        return (cacheDirectory != null) ?
                cacheDirectory : pluginsDirectory.resolve(DEFAULT_CACHE_DIRECTORY);
    }

    /**
     * Returns current {@link ManifestReader} instance
     *
     * @return current manifest reader instance
     */
    public ManifestReader getManifestReader() {
        return (manifestReader != null) ?
                manifestReader : new DefaultManifestReader();
    }

    /**
     * Returns current {@link DependencyChecker} instance
     *
     * @return current dependency checker instance
     */
    public DependencyChecker getDependencyChecker() {
        return (dependencyChecker != null) ?
                dependencyChecker : new DefaultDependencyChecker();
    }

    /**
     * Returns current {@link ClassesScanner} instance
     *
     * @return current classes scanner instance
     */
    public ClassesScanner getClassesScanner() {
        return (classesScanner != null) ?
                classesScanner : new DefaultClassesScanner(getCacheDirectory());
    }

    /**
     * Returns current {@link ResourcesScanner} instance
     *
     * @return current resources scanner instance
     */
    public ResourcesScanner getResourcesScanner() {
        return (resourcesScanner != null) ?
                resourcesScanner : new DefaultResourcesScanner(getCacheDirectory(), getResourcesPatterns());
    }

    /**
     * Returns a list of allowed resources globs
     *
     * @return a list of resources globs
     */
    public String[] getResourcesPatterns() {
        return resourcesPatterns;
    }

    /**
     * Returns a list extension points classes
     *
     * @return extension points list
     */
    public List<Class> getExtensionPoints() {
        return extensionPoints;
    }

    /**
     * Returns {@link PluginRegistry} storing information about loaded classes
     *
     * @return plugin registry with loaded classes
     */
    public PluginRegistry load() throws PluginException {

        List<Path> pluginFiles = getPluginsProvider().provide(getPluginsDirectory());

        PluginRegistry pluginRegistry = new PluginRegistryContainer();

        // Loading information about all plugins first
        for (Path pluginFile : pluginFiles) {
            PluginMetadata pluginMetadata = getManifestReader().read(pluginFile);
            pluginRegistry.addPlugin(pluginMetadata);
        }

        // Iterating over the entire plugin set, checking for dependency resolution problems and loading classes
        for (String pluginName : pluginRegistry.getPluginNames()) {
            Optional<PluginMetadata> pluginMetadata = pluginRegistry.getPlugin(pluginName);
            if (pluginMetadata.isPresent()) {
                getDependencyChecker().check(pluginRegistry, pluginMetadata.get());

                Map<Class, List<Class>> mapping = getClassesScanner().scan(
                        pluginMetadata.get().getPath(),
                        getExtensionPoints()
                );
                for (Class extensionPoint : mapping.keySet()) {
                    pluginRegistry.addImplementations(extensionPoint, mapping.get(extensionPoint));
                }

                List<Path> resources = getResourcesScanner().scan(pluginMetadata.get().getPath());
                pluginRegistry.addResources(pluginMetadata.get().getName(), resources);
            }
        }
        return pluginRegistry;
    }

}
