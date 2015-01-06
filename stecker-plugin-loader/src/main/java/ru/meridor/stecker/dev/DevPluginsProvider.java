package ru.meridor.stecker.dev;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.interfaces.PluginsProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DevPluginsProvider implements PluginsProvider {

    @Override
    public List<Path> provide(Path baseDirectory) throws PluginException {
        try {
            return Files.list(baseDirectory)
                    .filter(f -> Files.isDirectory(f) && f.toString().endsWith("plugin"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }
}
