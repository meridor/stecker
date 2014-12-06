package ru.meridor.stecker.interfaces;

import java.util.Optional;

/**
 * A dependency is just a pair of plugin name and version. When no version is present - that means any version.
 */
public interface Dependency {

    /**
     * Returns dependency name
     *
     * @return dependency name
     */
    String getName();

    /**
     * Returns dependency version if present
     *
     * @return dependency version if present and empty otherwise
     */
    Optional<String> getVersion();

    /**
     * Forces to override {@link Object#equals(Object)}
     *
     * @param anotherDependency dependency to compare to
     * @return true if equal and false otherwise
     */
    boolean equals(Object anotherDependency);

    /**
     * Forces to override {@link Object#hashCode()}
     *
     * @return instance hash code
     */
    int hashCode();

    /**
     * Returns dependency string representation
     *
     * @return dependency string representation
     */
    String toString();

}
