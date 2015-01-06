package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.interfaces.PluginsProvider;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultPluginsProvider implements PluginsProvider {

    private final String fileGlob;

    public DefaultPluginsProvider(String fileGlob) {
        this.fileGlob = fileGlob;
    }


    @Override
    public List<Path> provide(Path pluginsDirectory) throws PluginException {
        try {
            PathMatcher pathMatcher = FileSystems
                    .getDefault()
                    .getPathMatcher("glob:" + fileGlob);
            return Files.list(pluginsDirectory)
                    .filter(pathMatcher::matches)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }
}
