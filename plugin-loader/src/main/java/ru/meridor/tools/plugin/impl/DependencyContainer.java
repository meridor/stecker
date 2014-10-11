package ru.meridor.tools.plugin.impl;

import ru.meridor.tools.plugin.Dependency;

import java.util.Optional;

public class DependencyContainer implements Dependency {

    private final String name;

    private final String version;

    public DependencyContainer(String name) {
        this(name, null);
    }

    public DependencyContainer(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Dependency) &&
                getName().equals(((Dependency) obj).getName()) &&
                (
                        getVersion().equals(((Dependency) obj).getVersion()) ||
                                !getVersion().isPresent() ||
                                !((Dependency) obj).getVersion().isPresent()
                );
    }

    @Override
    public int hashCode() {
        return (getName() + getVersion()).hashCode();
    }

    @Override
    public String toString() {
        return String.format("Dependency(name=%s, version=%s)", getName(), getVersion().toString());
    }
}
