package ru.meridor.stecker.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginUtils {

    public static final String PLUGIN_IMPLEMENTATION_FILE = "plugin.jar";
    public static final String LIB_DIRECTORY = "lib";
    public static final String PLUGIN_IMPLEMENTATION_UNPACK_DIRECTORY = PLUGIN_IMPLEMENTATION_FILE + ".unpacked";
    private static final String JAR_FILE_EXTENSION = ".jar";

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
}
