package org.meridor.stecker.dev;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;
import org.meridor.stecker.impl.PluginMetadataContainer;
import org.meridor.stecker.interfaces.ManifestReader;

import java.nio.file.Files;
import java.nio.file.Path;

public class DevManifestReader implements ManifestReader {
    @Override
    public PluginMetadata read(Path pluginDirectory) throws PluginException {
        if (!Files.exists(pluginDirectory) || !Files.isDirectory(pluginDirectory)) {
            throw new PluginException(String.format("Specified path [%s] should be a directory", pluginDirectory));
        }
        String pluginName = pluginDirectory.getParent().relativize(pluginDirectory).toString();
        String pluginVersion = "dev";
        return new PluginMetadataContainer(pluginName, pluginVersion, pluginDirectory);
    }
}
