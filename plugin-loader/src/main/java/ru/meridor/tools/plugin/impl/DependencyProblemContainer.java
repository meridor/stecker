package ru.meridor.tools.plugin.impl;

import ru.meridor.tools.plugin.Dependency;
import ru.meridor.tools.plugin.DependencyProblem;

import java.util.List;

public class DependencyProblemContainer implements DependencyProblem {

    private final List<Dependency> missingDependencies;
    private final List<Dependency> conflictingDependencies;

    public DependencyProblemContainer(List<Dependency> missingDependencies, List<Dependency> conflictingDependencies) {
        this.missingDependencies = missingDependencies;
        this.conflictingDependencies = conflictingDependencies;
    }

    @Override
    public List<Dependency> getMissingDependencies() {
        return missingDependencies;
    }

    @Override
    public List<Dependency> getConflictingDependencies() {
        return conflictingDependencies;
    }
}
