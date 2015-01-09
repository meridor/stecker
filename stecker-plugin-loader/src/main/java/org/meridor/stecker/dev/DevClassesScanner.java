package org.meridor.stecker.dev;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.impl.PluginUtils;
import org.meridor.stecker.interfaces.ClassesScanner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DevClassesScanner implements ClassesScanner {

    private final BuildToolType buildToolType;

    public DevClassesScanner(BuildToolType buildToolType) {
        this.buildToolType = buildToolType;
    }

    @Override
    public Map<Class, List<Class>> scan(Path pluginDirectory, List<Class> extensionPoints) throws PluginException {
        if (Files.exists(pluginDirectory) && Files.isDirectory(pluginDirectory)) {
            try {
                ClassLoader classLoader = BuildToolClassLoaderFactory.getClassLoader(pluginDirectory, buildToolType);
                Path classesPath = BuildToolClassLoaderFactory.getClassesPath(pluginDirectory, buildToolType);
                return Files.exists(classesPath) ?
                        PluginUtils.getMatchingClasses(extensionPoints, classesPath, classLoader) :
                        Collections.emptyMap();
            } catch (Throwable e) {
                throw new PluginException(e);
            }
        }
        return Collections.emptyMap();
    }
}
