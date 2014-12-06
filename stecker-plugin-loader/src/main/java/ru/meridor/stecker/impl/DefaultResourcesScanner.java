package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.interfaces.ResourcesScanner;

import java.io.IOException;
import java.nio.file.*;
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
            return getMatchingFiles(unpackedPluginDirectory, patterns);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    private List<Path> getMatchingFiles(Path unpackedPluginDirectory, String[] patterns) throws IOException {
        List<PathMatcher> pathMatchers = Arrays.stream(patterns)
                .map(g -> FileSystems.getDefault().getPathMatcher(g))
                .collect(Collectors.toList());
        DirectoryStream<Path> matchingFilesStream = Files.newDirectoryStream(unpackedPluginDirectory, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                if (!Files.isRegularFile(entry)) {
                    return false;
                }
                for (PathMatcher pathMatcher : pathMatchers) {
                    if (pathMatcher.matches(entry)) {
                        return true;
                    }
                }
                return false;
            }
        });
        List<Path> matchingFiles = new ArrayList<>();
        for (Path matchingFile : matchingFilesStream) {
            matchingFiles.add(matchingFile);
        }
        return matchingFiles;
    }
}
