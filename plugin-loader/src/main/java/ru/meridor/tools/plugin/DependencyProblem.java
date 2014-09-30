package ru.meridor.tools.plugin;

import java.util.List;

/**
 * Provides information about dependency resolution problems
 */
public interface DependencyProblem {

    /**
     * Returns a list of dependencies that are required by a plugin but are not present for some reason
     * @return list of missing dependencies
     */
    List<Dependency> getMissingDependencies();

    /**
     * Returns a list of dependencies that conflict with one of plugins but are present for some reason
     * @return list of conflicting plugins
     */
    List<Dependency> getConflictingDependencies();

}
