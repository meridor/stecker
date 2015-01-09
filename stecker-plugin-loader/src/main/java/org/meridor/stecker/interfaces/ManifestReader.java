package org.meridor.stecker.interfaces;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;

import java.nio.file.Path;

public interface ManifestReader {

    /**
     * Reads jar file manifest and returns plugin information stored there
     *
     * @param pluginFile plugin file to process
     * @return plugin information in {@link org.meridor.stecker.PluginMetadata} format
     */
    PluginMetadata read(Path pluginFile) throws PluginException;

}
