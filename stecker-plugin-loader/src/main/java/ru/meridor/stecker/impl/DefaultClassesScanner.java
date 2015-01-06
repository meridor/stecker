package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.interfaces.ClassesScanner;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DefaultClassesScanner implements ClassesScanner {

    private final Path cacheDirectory;

    public DefaultClassesScanner(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    public Map<Class, List<Class>> scan(Path pluginFile, List<Class> extensionPoints) throws PluginException {
        try {
            Path unpackedPluginDirectory = PluginUtils.unpackPlugin(pluginFile, cacheDirectory);
            Path pluginImplementationDirectory = PluginUtils.getPluginImplementationDirectory(unpackedPluginDirectory);

            ClassLoader classLoader = getClassLoader(unpackedPluginDirectory, pluginImplementationDirectory);
            return PluginUtils.getMatchingClasses(extensionPoints, pluginImplementationDirectory, classLoader);

        } catch (Throwable e) {
            throw new PluginException(e);
        }
    }

    private ClassLoader getClassLoader(Path unpackedPluginDirectory, Path pluginImplementationDirectory) throws PluginException {
        Path libDirectory = unpackedPluginDirectory.resolve(PluginUtils.LIB_DIRECTORY);
        return PluginUtils.getClassLoader(pluginImplementationDirectory, libDirectory);
    }

}
