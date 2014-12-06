package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.PluginMetadata;
import ru.meridor.stecker.VersionRelation;
import ru.meridor.stecker.interfaces.Dependency;
import ru.meridor.stecker.interfaces.DependencyChecker;
import ru.meridor.stecker.interfaces.PluginsAware;
import ru.meridor.stecker.interfaces.VersionComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultDependencyChecker implements DependencyChecker {

    @Override
    public void check(PluginsAware pluginRegistry, PluginMetadata pluginMetadata) throws PluginException {

        List<Dependency> missingRequiredDependencies = new ArrayList<>();
        requiredDependencies:
        for (Dependency requiredDependency : pluginMetadata.getRequiredDependencies()) {
            Optional<PluginMetadata> requiredDependencyCandidate = pluginRegistry
                    .getPlugin(requiredDependency.getName());
            if (!requiredDependencyCandidate.isPresent()) {
                missingRequiredDependencies.add(requiredDependency);
            } else {
                switch (compareVersions(requiredDependency, requiredDependencyCandidate.get())) {
                    case EQUAL:
                    case IN_RANGE:
                        continue requiredDependencies;
                    default:
                        missingRequiredDependencies.add(requiredDependency);
                }
            }
        }

        List<Dependency> presentConflictingDependencies = new ArrayList<>();
        for (Dependency conflictingDependency : pluginMetadata.getConflictingDependencies()) {
            Optional<PluginMetadata> conflictingDependencyCandidate = pluginRegistry
                    .getPlugin(conflictingDependency.getName());
            if (conflictingDependencyCandidate.isPresent()) {
                switch (compareVersions(conflictingDependency, conflictingDependencyCandidate.get())) {
                    case EQUAL:
                    case IN_RANGE:
                        presentConflictingDependencies.add(conflictingDependency);
                }
            }
        }

        if (!missingRequiredDependencies.isEmpty() || !presentConflictingDependencies.isEmpty()) {
            throw new PluginException(
                    String.format(
                            "Dependency issue: %d missing dependencies, %s conflicting dependencies",
                            missingRequiredDependencies.size(),
                            presentConflictingDependencies.size()
                    )
            ).withPlugin(pluginMetadata)
                    .withDependencyProblem(
                            new DependencyProblemContainer(missingRequiredDependencies, presentConflictingDependencies)
                    );
        }

    }

    private VersionRelation compareVersions(Dependency requiredDependency, PluginMetadata dependencyCandidate) {
        Optional<String> requiredVersion = requiredDependency.getVersion();
        Optional<String> actualVersion = Optional.ofNullable(dependencyCandidate.getVersion());
        return getVersionComparator().compare(requiredVersion, actualVersion);
    }

    private VersionComparator getVersionComparator() {
        return new DefaultVersionComparator();
    }
}
