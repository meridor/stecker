package ru.meridor.tools.plugin;

import java.nio.file.Path;
import java.util.Optional;

/**
 * An generic exception thrown when something goes wrong with plugins
 */
public class PluginException extends Exception {

    private Optional<DependencyProblem> dependencyProblem = Optional.empty();

    private Optional<Path> pluginFile = Optional.empty();

    private Optional<PluginMetadata> pluginMetadata = Optional.empty();

    public PluginException(Throwable e) {
        super(e);
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable e) {
        super(message, e);
    }

    public PluginException withDependencyProblem(DependencyProblem dependencyProblem) {
        this.dependencyProblem = Optional.ofNullable(dependencyProblem);
        return this;
    }

    public PluginException withPlugin(PluginMetadata pluginMetadata) {
        this.pluginMetadata = Optional.ofNullable(pluginMetadata);
        return this;
    }

    public PluginException withPluginFile(Path pluginFile) {
        this.pluginFile = Optional.ofNullable(pluginFile);
        return this;
    }

    /**
     * Returns dependency problem if any
     * @return dependency problem
     */
    public Optional<DependencyProblem> getDependencyProblem(){
        return dependencyProblem;
    }

    /**
     * Returns plugin affected
     * @return plugin metadata
     */
    public Optional<PluginMetadata> getPluginMetadata() {
        return pluginMetadata;
    }

    /**
     * Returns plugin file affected
     * @return plugin file
     */
    public Optional<Path> getPluginFile() {
        return pluginFile;
    }
}
