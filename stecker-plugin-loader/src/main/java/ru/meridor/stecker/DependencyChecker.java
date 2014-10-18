package ru.meridor.stecker;

/**
 * Checks whether all plugin dependencies are satisfied and no conflicting dependencies are present
 */
public interface DependencyChecker {

    /**
     * Checks dependencies and throws {@link PluginException} with {@link DependencyProblem} in case of issues
     *
     * @param pluginRegistry plugin registry object filled with data about all plugins
     * @param pluginMetadata checked plugin metadata
     * @throws PluginException
     */
    void check(PluginRegistry pluginRegistry, PluginMetadata pluginMetadata) throws PluginException;

}
