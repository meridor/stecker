package ru.meridor.stecker.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginUtils {

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

        return pluginStorageDirectory;
    }

    private static void unpackJar(Path pluginFile, Path pluginStorageDirectory) throws IOException {

        try (JarFile jarFile = new JarFile(pluginFile.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                Path outputPath = Paths.get(pluginStorageDirectory.toUri()).resolve(entry.getName());
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
        FileTime fileLastModificationTime = Files.readAttributes(file, BasicFileAttributes.class).lastModifiedTime();
        FileTime anotherFileLastModificationTime = Files.readAttributes(anotherFile, BasicFileAttributes.class).lastModifiedTime();
        return fileLastModificationTime.compareTo(anotherFileLastModificationTime) > 0;
    }

}
