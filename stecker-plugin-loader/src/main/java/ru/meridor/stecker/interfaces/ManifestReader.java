package ru.meridor.stecker.interfaces;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.PluginMetadata;

import java.nio.file.Path;

public interface ManifestReader {

    /**
     * Reads jar file manifest and returns plugin information stored there
     *
     * @param pluginFile plugin file to process
     * @return plugin information in {@link ru.meridor.stecker.PluginMetadata} format
     */
    PluginMetadata read(Path pluginFile) throws PluginException;

}
