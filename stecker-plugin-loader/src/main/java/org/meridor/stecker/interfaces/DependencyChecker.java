package org.meridor.stecker.interfaces;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;

/**
 * Checks whether all plugin dependencies are satisfied and no conflicting dependencies are present
 */
public interface DependencyChecker {

    /**
     * Checks dependencies and throws {@link org.meridor.stecker.PluginException} with {@link DependencyProblem} in case of issues
     *
     * @param pluginRegistry plugin registry object filled with data about all plugins
     * @param pluginMetadata checked plugin metadata
     * @throws org.meridor.stecker.PluginException
     */
    void check(PluginsAware pluginRegistry, PluginMetadata pluginMetadata) throws PluginException;

}
