package org.meridor.stecker.interfaces;

import org.meridor.stecker.PluginMetadata;

import java.util.List;
import java.util.Optional;

public interface PluginsAware {

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

}
