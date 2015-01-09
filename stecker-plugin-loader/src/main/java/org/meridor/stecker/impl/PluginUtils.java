package org.meridor.stecker.impl;

import org.meridor.stecker.PluginException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginUtils {

    public static final String PLUGIN_IMPLEMENTATION_FILE = "plugin.jar";
    public static final String LIB_DIRECTORY = "lib";
    public static final String PLUGIN_IMPLEMENTATION_UNPACK_DIRECTORY = PLUGIN_IMPLEMENTATION_FILE + ".unpacked";
    private static final String JAR_FILE_EXTENSION = ".jar";
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final String JAR_DIRECTORY_SEPARATOR = "/";

    public static Path unpackPlugin(Path pluginFile, Path cacheDirectory) throws IOException {
        String pluginName = pluginFile.getFileName().toString().replace(JAR_FILE_EXTENSION, "");
        Path pluginStorageDirectory = cacheDirectory.resolve(pluginName);

        if (Files.exists(pluginStorageDirectory)) {
            if (!Files.isDirectory(pluginStorageDirectory)) {
                throw new IOException("Plugin cache directory is not a directory");
            }
            if (fileIsNewerThan(pluginStorageDirectory, pluginFile)) {
                return pluginStorageDirectory;
            }
            FileSystemHelper.removeDirectory(pluginStorageDirectory);
        }

        Files.createDirectories(pluginStorageDirectory);
        unpackJar(pluginFile, pluginStorageDirectory);

        Path pluginImplementationFilePath = pluginStorageDirectory.resolve(PLUGIN_IMPLEMENTATION_FILE);
        Path pluginImplementationDirectory = getPluginImplementationDirectory(pluginStorageDirectory);

        Files.createDirectories(pluginImplementationDirectory);
        unpackJar(pluginImplementationFilePath, pluginImplementationDirectory);
        Files.delete(pluginImplementationFilePath);

        return pluginStorageDirectory;
    }

    private static void unpackJar(Path pluginFile, Path pluginStorageDirectory) throws IOException {

        try (JarFile jarFile = new JarFile(pluginFile.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(FileSystems.getDefault().getSeparator()) && entryName.length() > 1) {
                    entryName = entryName.substring(1);
                }
                Path outputPath = Paths.get(pluginStorageDirectory.toUri()).resolve(entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                    continue;
                }

                Path parentDirectory = outputPath.getParent();
                if (!Files.exists(parentDirectory)) {
                    Files.createDirectories(parentDirectory);
                }

                try (
                        InputStream is = jarFile.getInputStream(entry);
                        OutputStream os = Files.newOutputStream(outputPath)
                ) {
                    while (is.available() > 0) {
                        os.write(is.read());
                    }
                }

            }
        }
    }

    private static boolean fileIsNewerThan(Path file, Path anotherFile) throws IOException {
        FileTime fileLastModificationTime = getLastModificationTime(file);
        FileTime anotherFileLastModificationTime = getLastModificationTime(anotherFile);
        return fileLastModificationTime.compareTo(anotherFileLastModificationTime) > 0;
    }

    public static FileTime getLastModificationTime(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime();
    }

    public static Path getPluginImplementationDirectory(Path unpackedPluginDirectory) {
        return unpackedPluginDirectory.resolve(PLUGIN_IMPLEMENTATION_UNPACK_DIRECTORY);
    }

    public static List<Path> getMatchingFiles(Path pluginImplementationDirectory, String[] patterns) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        List<PathMatcher> pathMatchers = Arrays.stream(patterns)
                .map(FileSystems.getDefault()::getPathMatcher)
                .collect(Collectors.toList());

        for (PathMatcher pathMatcher : pathMatchers) {
            List<Path> matchingPaths = Files.list(pluginImplementationDirectory)
                    .filter(pathMatcher::matches)
                    .collect(Collectors.toList());
            matchingFiles.addAll(matchingPaths);
        }

        return matchingFiles;
    }


    public static Map<Class, List<Class>> getMatchingClasses(List<Class> extensionPoints, Path pluginImplementationDirectory, ClassLoader classLoader) throws Exception {
        Map<Class, List<Class>> matchingClasses = new HashMap<>();

        List<Path> classFiles = new ArrayList<>();
        Files.walkFileTree(pluginImplementationDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                if (file.toString().toLowerCase().endsWith(CLASS_FILE_EXTENSION)) {
                    classFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        for (Path classFile : classFiles) {
            String className = getClassName(pluginImplementationDirectory, classFile);
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

        return matchingClasses;
    }

    private static String getClassName(Path pluginImplementationDirectory, Path classFile) {
        String className = pluginImplementationDirectory
                .relativize(classFile)
                .toString()
                .replace(JAR_DIRECTORY_SEPARATOR, PACKAGE_SEPARATOR)
                .replace(CLASS_FILE_EXTENSION, "");
        if (className.startsWith(PACKAGE_SEPARATOR) && className.length() > 1) {
            className = className.substring(1);
        }
        return className;
    }

    private static boolean isAnnotatedWith(Class<?> currentClass, Class<?> extensionPoint) {
        return
                extensionPoint.isAnnotation() &&
                        currentClass.isAnnotationPresent(extensionPoint.asSubclass(Annotation.class));
    }


    private static boolean isSubclassOf(Class<?> currentClass, Class<?> extensionPoint) {
        return extensionPoint.isAssignableFrom(currentClass);
    }

    public static ClassLoader getClassLoader(Path classesPath, Path dependenciesPath) throws PluginException {
        try {
            List<URL> urls = new ArrayList<>();
            if (dependenciesPath != null && Files.exists(dependenciesPath) && Files.isDirectory(dependenciesPath)) {
                List<URI> uris = Files.list(dependenciesPath)
                        .filter(Files::isRegularFile)
                        .map(Path::toUri)
                        .collect(Collectors.toList());
                for (URI uri : uris) {
                    urls.add(uri.toURL());
                }
            }

            urls.add(classesPath.toUri().toURL());

            return new URLClassLoader(urls.toArray(new URL[urls.size()]));
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }


}
