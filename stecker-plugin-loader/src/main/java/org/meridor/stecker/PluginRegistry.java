package org.meridor.stecker;

import org.meridor.stecker.interfaces.ImplementationsAware;
import org.meridor.stecker.interfaces.PluginsAware;
import org.meridor.stecker.interfaces.ResourcesAware;

import java.nio.file.Path;
import java.util.List;

/**
 * Stores all information about loaded plugins
 */
public interface PluginRegistry extends PluginsAware, ImplementationsAware, ResourcesAware {

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
     * Add resources for specific plugin
     *
     * @param pluginName plugin name
     * @param resources  plugin resources
     */
    void addResources(String pluginName, List<Path> resources);

}
