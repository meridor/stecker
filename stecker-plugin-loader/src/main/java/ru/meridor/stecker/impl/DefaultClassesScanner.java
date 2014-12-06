package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.interfaces.ClassesScanner;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class DefaultClassesScanner implements ClassesScanner {

    public static final String LIB_DIRECTORY = "lib";
    public static final String PLUGIN_CLASSES_FILE = "plugin.jar";
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final String JAR_DIRECTORY_SEPARATOR = "/";
    private static final String PACKAGE_SEPARATOR = ".";
    private final Path cacheDirectory;

    public DefaultClassesScanner(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    public Map<Class, List<Class>> scan(Path pluginFile, List<Class> extensionPoints) throws PluginException {
        try {
            Path unpackedPluginDirectory = PluginUtils.unpackPlugin(pluginFile, cacheDirectory);
            Path pluginJarFile = getPluginJarPath(unpackedPluginDirectory);
            ClassLoader classLoader = getClassLoader(unpackedPluginDirectory);
            return getMatchingClasses(extensionPoints, pluginJarFile, classLoader);

        } catch (Throwable e) {
            throw new PluginException(e);
        }
    }

    private Path getPluginJarPath(Path unpackedPluginDirectory) {
        return unpackedPluginDirectory.resolve(PLUGIN_CLASSES_FILE);
    }

    private ClassLoader getClassLoader(Path unpackedPluginDirectory) throws PluginException {
        try {
            Path libDirectory = unpackedPluginDirectory.resolve(LIB_DIRECTORY);
            List<URL> urls = new ArrayList<>();
            if (Files.exists(libDirectory) && Files.isDirectory(libDirectory)) {
                List<URI> uris = Files.list(libDirectory)
                        .filter(Files::isRegularFile)
                        .map(Path::toUri)
                        .collect(Collectors.toList());
                for (URI uri : uris) {
                    urls.add(uri.toURL());
                }
            }
            Path pluginJarPath = getPluginJarPath(unpackedPluginDirectory);
            urls.add(pluginJarPath.toUri().toURL());
            return new URLClassLoader(urls.toArray(new URL[urls.size()]));
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    private Map<Class, List<Class>> getMatchingClasses(List<Class> extensionPoints, Path pluginJarFile, ClassLoader classLoader) throws Exception {
        Map<Class, List<Class>> matchingClasses = new HashMap<>();
        JarFile jar = new JarFile(pluginJarFile.toFile());
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(CLASS_FILE_EXTENSION) && !entry.isDirectory()) {
                String className = entry.getName()
                        .replace(JAR_DIRECTORY_SEPARATOR, PACKAGE_SEPARATOR)
                        .replace(CLASS_FILE_EXTENSION, "");
                if (className.startsWith(PACKAGE_SEPARATOR) && className.length() > 1) {
                    className = className.substring(1);
                }
                Class<?> currentClass = Class.forName(className, true, classLoader);
                for (Class<?> extensionPoint : extensionPoints) {
                    if (isSubclassOf(currentClass, extensionPoint) || isAnnotatedWith(currentClass, extensionPoint)) {
                        if (!matchingClasses.containsKey(extensionPoint)) {
                            matchingClasses.put(extensionPoint, new ArrayList<>());
                        }
                        matchingClasses.get(extensionPoint).add(currentClass);
                    }
                }
            }
        }
        return matchingClasses;
    }

    private boolean isAnnotatedWith(Class<?> currentClass, Class<?> extensionPoint) {
        return
                extensionPoint.isAnnotation() &&
                        currentClass.isAnnotationPresent(extensionPoint.asSubclass(Annotation.class));
    }


    private boolean isSubclassOf(Class<?> currentClass, Class<?> extensionPoint) {
        return extensionPoint.isAssignableFrom(currentClass);
    }
}
