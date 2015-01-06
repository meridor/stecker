package ru.meridor.stecker.dev;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.PluginMetadata;
import ru.meridor.stecker.impl.PluginMetadataContainer;
import ru.meridor.stecker.interfaces.ManifestReader;

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
