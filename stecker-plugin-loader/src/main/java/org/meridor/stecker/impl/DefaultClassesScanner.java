package org.meridor.stecker.impl;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.interfaces.ClassesScanner;
import org.meridor.stecker.interfaces.PluginImplementationsAware;
import org.meridor.stecker.interfaces.ScanResult;

import java.nio.file.Path;
import java.util.List;

public class DefaultClassesScanner implements ClassesScanner {

    private final Path cacheDirectory;

    public DefaultClassesScanner(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    public ScanResult scan(Path pluginFile, List<Class> extensionPoints) throws PluginException {
        try {
            Path unpackedPluginDirectory = PluginUtils.unpackPlugin(pluginFile, cacheDirectory);
            Path pluginImplementationDirectory = PluginUtils.getPluginImplementationDirectory(unpackedPluginDirectory);

            ClassLoader classLoader = getClassLoader(unpackedPluginDirectory, pluginImplementationDirectory);
            PluginImplementationsAware pluginImplementationsAware = PluginUtils.getMatchingClasses(extensionPoints, pluginImplementationDirectory, classLoader);
            return new DefaultScanResult(classLoader, pluginImplementationsAware);

        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    private ClassLoader getClassLoader(Path unpackedPluginDirectory, Path pluginImplementationDirectory) throws PluginException {
        Path libDirectory = unpackedPluginDirectory.resolve(PluginUtils.LIB_DIRECTORY);
        return PluginUtils.getClassLoader(pluginImplementationDirectory, libDirectory);
    }

}
