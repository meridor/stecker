package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.interfaces.ResourcesScanner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DefaultResourcesScanner implements ResourcesScanner {

    private final Path cacheDirectory;
    private final String[] patterns;

    public DefaultResourcesScanner(Path cacheDirectory, String[] patterns) {
        this.cacheDirectory = cacheDirectory;
        this.patterns = patterns;
    }

    @Override
    public List<Path> scan(Path pluginFile) throws PluginException {
        try {
            Path unpackedPluginDirectory = PluginUtils.unpackPlugin(pluginFile, cacheDirectory);
            Path pluginImplementationDirectory = PluginUtils.getPluginImplementationDirectory(unpackedPluginDirectory);
            return PluginUtils.getMatchingFiles(pluginImplementationDirectory, patterns);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

}
