package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.interfaces.ResourcesScanner;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            return getMatchingFiles(pluginImplementationDirectory, patterns);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    private List<Path> getMatchingFiles(Path pluginImplementationDirectory, String[] patterns) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        List<PathMatcher> pathMatchers = Arrays.stream(patterns)
                .map(FileSystems.getDefault()::getPathMatcher)
                .collect(Collectors.toList());

        for (PathMatcher pathMatcher : pathMatchers) {
            List<Path> matchingPaths = Files.list(pluginImplementationDirectory)
                    .filter(pathMatcher::matches)
                    .collect(Collectors.toList());
            matchingFiles.addAll(matchingPaths);
        }

        return matchingFiles;
    }
}
