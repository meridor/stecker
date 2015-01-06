package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginMetadata;
import ru.meridor.stecker.interfaces.Dependency;

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
    private String description;
    private String maintainer;
    private Dependency provides;

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
        return Optional.ofNullable(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Optional<String> getMaintainer() {
        return Optional.ofNullable(maintainer);
    }

    public void setMaintainer(String maintainer) {
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
        return Optional.ofNullable(provides);
    }

    public void setProvidedDependency(Dependency provides) {
        this.provides = provides;
    }

    public void addRequiredDependencies(List<Dependency> dependencies) {
        this.depends.addAll(dependencies);
    }

    public void addConflictingDependencies(List<Dependency> dependencies) {
        this.conflicts.addAll(dependencies);
    }

}
