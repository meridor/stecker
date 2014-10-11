package ru.meridor.tools.plugin;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Stores metadata of a single plugin
 */
public interface PluginMetadata {

    /**
     * Returns unique plugin name
     *
     * @return free-form string with name
     */
    String getName();

    /**
     * Returns plugin version
     *
     * @return free-form string with version
     */
    String getVersion();

    /**
     * Returns {@link Path} object corresponding to plugin file
     *
     * @return plugin {@link Path} object
     */
    Path getPath();

    /**
     * Returns {@link Dependency} object corresponding to this plugin
     *
     * @return dependency object
     */
    Dependency getDependency();

    /**
     * Returns date when plugin was released
     *
     * @return local date or empty if not set
     */
    Optional<ZonedDateTime> getDate();

    /**
     * Returns detailed plugin description
     *
     * @return description or empty if not set
     */
    Optional<String> getDescription();

    /**
     * Returns maintainer email and name
     *
     * @return email and name in the format: <b>John Smith <john.smith@example.com></b> or empty if not set
     */
    Optional<String> getMaintainer();

    /**
     * Returns a list of plugins required to make this plugin work
     *
     * @return possibly empty list with dependencies
     */
    List<Dependency> getRequiredDependencies();

    /**
     * Returns a list of plugins that should be never installed simultaneously with this plugin
     *
     * @return possibly empty list with conflicting dependencies
     */
    List<Dependency> getConflictingDependencies();

    /**
     * Returns a meta dependency which is provided by this plugin
     *
     * @return meta dependency or empty if not set
     */
    Optional<Dependency> getProvidedDependency();

}
