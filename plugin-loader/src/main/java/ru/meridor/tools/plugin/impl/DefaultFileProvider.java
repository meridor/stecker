package ru.meridor.tools.plugin.impl;

import ru.meridor.tools.plugin.FileProvider;
import ru.meridor.tools.plugin.PluginException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultFileProvider implements FileProvider {

    private final File directory;

    public DefaultFileProvider(File directory) {
        this.directory = directory;
    }

    @Override
    public List<File> provide() throws PluginException {
        if (directory == null || !directory.exists() || !directory.isDirectory()){
            return Collections.emptyList();
        }
        File[] files = directory.listFiles();
        return (files != null) ? Arrays.asList(files) : Collections.emptyList();
    }
}
