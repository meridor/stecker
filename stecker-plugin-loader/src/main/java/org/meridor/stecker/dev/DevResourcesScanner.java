package org.meridor.stecker.dev;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.impl.PluginUtils;
import org.meridor.stecker.interfaces.ResourcesScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class DevResourcesScanner implements ResourcesScanner {

    private final String[] patterns;

    public DevResourcesScanner(String[] patterns) {
        this.patterns = patterns;
    }

    @Override
    public List<Path> scan(Path pluginDirectory) throws PluginException {
        Path resourcesPath = pluginDirectory.resolve("src").resolve("main").resolve("resources");
        if (Files.exists(resourcesPath) && Files.isDirectory(resourcesPath)) {
            try {
                return PluginUtils.getMatchingFiles(resourcesPath, patterns);
            } catch (IOException e) {
                throw new PluginException(e);
            }
        }
        return Collections.emptyList();
    }
}
