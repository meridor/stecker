package ru.meridor.tools.plugin;

import java.util.List;
import java.util.Optional;

/**
 * Stores all information about loaded plugins
 */
public interface PluginRegistry {

    /**
     * Add extension point implementations to registry
     *
     * @param extensionPoint        extension point class
     * @param implementationClasses a list of implementation classes
     */
    void addImplementations(Class extensionPoint, List<Class> implementationClasses);

    /**
     * Adds information about new plugin
     *
     * @param pluginMetadata plugin metadata object
     * @throws PluginException when another plugin provides the same virtual dependency
     */
    void addPlugin(PluginMetadata pluginMetadata) throws PluginException;

    /**
     * Returns information about plugin by its plugin name
     *
     * @param pluginName plugin name to process
     * @return information about plugin or empty if not present
     */
    Optional<PluginMetadata> getPlugin(String pluginName);

    /**
     * Returns a list of dependencies in the registry
     *
     * @return a list of dependencies
     */
    List<String> getPluginNames();

    /**
     * Returns a list of valid extension points
     *
     * @return a list of extension points
     */
    List<Class> getExtensionPoints();

    /**
     * Returns classes implementing extension point
     *
     * @param extensionPoint extension point class
     * @return a list of implementation classes
     */
    List<Class> getImplementations(Class extensionPoint);

}
