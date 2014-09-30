package ru.meridor.tools.plugin;

import java.io.File;

public interface ManifestReader {

    /**
     * Reads jar file manifest and returns plugin information stored there
     * @param pluginFile plugin file to process
     * @return plugin information in {@link PluginMetadata} format
     */
    PluginMetadata read(File pluginFile) throws PluginException;

}
