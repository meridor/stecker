package ru.meridor.stecker;

/**
 * Plugin is a jar file with additional fields in its MANIFEST.mf. These fields store plugin name,
 * version, date, dependencies list and so on. See {@link PluginMetadata} for details on existing manifest fields.
 */
public interface Plugin {

    /**
     * Plugin initialization logic
     *
     * @throws PluginException
     */
    default void init() throws PluginException {
    }

    /**
     * Plugin destruction logic
     *
     * @throws PluginException
     */
    default void destroy() throws PluginException {
    }

}
