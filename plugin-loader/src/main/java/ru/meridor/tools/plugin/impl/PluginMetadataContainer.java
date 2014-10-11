package ru.meridor.tools.plugin.impl;

import ru.meridor.tools.plugin.Dependency;
import ru.meridor.tools.plugin.PluginMetadata;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PluginMetadataContainer implements PluginMetadata {

    private final String name;

    private final String version;

    private final Path filePath;
    private final List<Dependency> depends = new ArrayList<>();
    private final List<Dependency> conflicts = new ArrayList<>();
    private Optional<ZonedDateTime> date;
    private Optional<String> description;
    private Optional<String> maintainer;
    private Optional<Dependency> provides;

    public PluginMetadataContainer(String name, String version, Path filePath) {
        this.name = name;
        this.version = version;
        this.filePath = filePath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Path getPath() {
        return filePath;
    }

    @Override
    public Dependency getDependency() {
        return new DependencyContainer(getName(), getVersion());
    }

    @Override
    public Optional<ZonedDateTime> getDate() {
        return date;
    }

    public void setDate(Optional<ZonedDateTime> date) {
        this.date = date;
    }

    @Override
    public Optional<String> getDescription() {
        return description;
    }

    public void setDescription(Optional<String> description) {
        this.description = description;
    }

    @Override
    public Optional<String> getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(Optional<String> maintainer) {
        this.maintainer = maintainer;
    }

    @Override
    public List<Dependency> getRequiredDependencies() {
        return depends;
    }

    @Override
    public List<Dependency> getConflictingDependencies() {
        return conflicts;
    }

    @Override
    public Optional<Dependency> getProvidedDependency() {
        return provides;
    }

    public void setProvidedDependency(Optional<Dependency> provides) {
        this.provides = provides;
    }

    public void addRequiredDependencies(List<Dependency> dependencies) {
        this.depends.addAll(dependencies);
    }

    public void addConflictingDependencies(List<Dependency> dependencies) {
        this.conflicts.addAll(dependencies);
    }

}
