package ru.meridor.tools.plugin;

import java.util.Optional;

/**
 * An generic exception thrown when something goes wrong with plugins
 */
public class PluginException extends Exception {

    private Optional<DependencyProblem> dependencyProblem = Optional.empty();

    public PluginException(Throwable e) {
        super(e);
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException withDependencyProblem(DependencyProblem dependencyProblem) {
        this.dependencyProblem = Optional.ofNullable(dependencyProblem);
        return this;
    }

    /**
     * Returns dependency problem if any
     * @return dependency problem
     */
    public Optional<DependencyProblem> getDependencyProblem(){
        return dependencyProblem;
    }
}
