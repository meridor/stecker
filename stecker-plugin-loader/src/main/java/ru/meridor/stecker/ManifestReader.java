package ru.meridor.stecker;

import java.nio.file.Path;

public interface ManifestReader {

    /**
     * Reads jar file manifest and returns plugin information stored there
     *
     * @param pluginFile plugin file to process
     * @return plugin information in {@link PluginMetadata} format
     */
    PluginMetadata read(Path pluginFile) throws PluginException;

}
