package org.meridor.stecker.dev;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.impl.ClassesRegistry;
import org.meridor.stecker.impl.DefaultScanResult;
import org.meridor.stecker.impl.PluginUtils;
import org.meridor.stecker.interfaces.ClassesScanner;
import org.meridor.stecker.interfaces.PluginImplementationsAware;
import org.meridor.stecker.interfaces.ScanResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class DevClassesScanner implements ClassesScanner {

    private final BuildToolType buildToolType;

    public DevClassesScanner(BuildToolType buildToolType) {
        this.buildToolType = buildToolType;
    }

    @Override
    public ScanResult scan(Path pluginDirectory, List<Class> extensionPoints) throws PluginException {
        if (Files.exists(pluginDirectory) && Files.isDirectory(pluginDirectory)) {
            try {
                ClassLoader classLoader = BuildToolClassLoaderFactory.getClassLoader(pluginDirectory, buildToolType);
                Path classesPath = BuildToolClassLoaderFactory.getClassesPath(pluginDirectory, buildToolType);
                PluginImplementationsAware pluginImplementationsAware =
                        Files.exists(classesPath) ?
                                PluginUtils.getMatchingClasses(extensionPoints, classesPath, classLoader) :
                                new ClassesRegistry(Collections.emptyMap());
                return new DefaultScanResult(classLoader, pluginImplementationsAware);
            } catch (Exception e) {
                throw new PluginException(e);
            }
        }
        return new DefaultScanResult(getClass().getClassLoader(), new ClassesRegistry(Collections.emptyMap()));
    }
}
