package ru.meridor.tools.plugin.impl;

import ru.meridor.tools.plugin.ClassesScanner;
import ru.meridor.tools.plugin.Plugin;
import ru.meridor.tools.plugin.PluginException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DefaultClassesScanner implements ClassesScanner {

    public static final String CLASS_FILE_EXTENSION = ".class";
    public static final String JAR_DIRECTORY_SEPARATOR = "/";
    public static final String PACKAGE_SEPARATOR = ".";

    private final ClassLoader classLoader;

    public DefaultClassesScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Map<Class, List<Class>> scan(List<Class> extensionPoints, File pluginFile) throws PluginException {
        try {
            extensionPoints.add(Plugin.class); //Plugin base class is always an extension point
            Map<Class, List<Class>> matchingClasses = new HashMap<>();
            JarFile jar = getJarFile(pluginFile);

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(CLASS_FILE_EXTENSION) && !entry.isDirectory()) {
                    String className = entry.getName()
                            .replace(JAR_DIRECTORY_SEPARATOR, PACKAGE_SEPARATOR)
                            .replace(CLASS_FILE_EXTENSION, "");
                    Class<?> currentClass = Class.forName(className, true, getClassLoader());

                    for (Class<?> extensionPoint: extensionPoints) {
                        if (extensionPoint.isAssignableFrom(currentClass)){
                            if (!matchingClasses.containsKey(extensionPoint)){
                                matchingClasses.put(extensionPoint, new ArrayList<>());
                            }
                            matchingClasses.get(extensionPoint).add(currentClass);
                        }
                    }
                }
            }
            return matchingClasses;

        } catch (Throwable e) {
            throw new PluginException(e);
        }
    }

    public JarFile getJarFile(File pluginFile) throws IOException {
        return new JarFile(pluginFile);
    }
}
