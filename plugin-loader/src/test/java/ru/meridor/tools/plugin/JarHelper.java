package ru.meridor.tools.plugin;

import ru.meridor.tools.plugin.impl.DefaultClassesScanner;
import ru.meridor.tools.plugin.impl.data.LibraryClass;
import ru.meridor.tools.plugin.impl.data.PluginImpl;
import ru.meridor.tools.plugin.impl.data.TestExtensionPointImpl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class JarHelper {

    private static final String CLASS_EXTENSION = ".class";

    private static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();

    private static final String DEPENDENCY_JAR_NAME = "dependency.jar";

    private JarHelper() {
    }

    private static Path createJarFile(Path directory, String name, Optional<Manifest> manifest, Class[] classesToAdd, Map<String, Path> filesToAdd) throws Exception {
        Path outputFile = directory.resolve(name);
        OutputStream fileOutputStream = Files.newOutputStream(outputFile);
        try (JarOutputStream outputStream = manifest.isPresent() ?
                new JarOutputStream(fileOutputStream, manifest.get()) :
                new JarOutputStream(fileOutputStream, new Manifest())
        ) {
            for (Class currentClass : classesToAdd) {
                addClass(outputStream, currentClass);
            }
            for (String entryName : filesToAdd.keySet()) {
                Path file = filesToAdd.get(entryName);
                addPath(outputStream, file, entryName);
            }
        }
        return outputFile;
    }

    private static void addClass(JarOutputStream outputStream, Class currentClass) throws Exception {
        String resourceName = FILE_SEPARATOR + currentClass.getCanonicalName().replace(".", FILE_SEPARATOR) + CLASS_EXTENSION;
        Path classFile = Paths.get(currentClass.getResource(resourceName).toURI());
        addPath(outputStream, classFile, resourceName);
    }

    private static void addPath(JarOutputStream outputStream, Path path, String entryName) throws Exception {
        outputStream.putNextEntry(new JarEntry(entryName));
        if (!Files.isDirectory(path)) {
            try (InputStream inputStream = Files.newInputStream(path)) {
                while (inputStream.available() > 0) {
                    outputStream.write(inputStream.read());
                }
            }
        }
        outputStream.closeEntry();
    }

    public static Path createTestPluginFile(String pluginName, Path directory, Optional<Manifest> manifest) throws Exception {

        Path[] paths = createUnpackedTestPluginFile(directory);
        Path pluginJar = paths[0];
        Path libDirectory = paths[1];
        Path dependencyFile = paths[2];

        Path testPlugin = JarHelper.createJarFile(
                directory,
                pluginName + ".jar",
                manifest,
                new Class[0],
                new HashMap<String, Path>() {
                    {
                        put(DefaultClassesScanner.PLUGIN_CLASSES_FILE, pluginJar);
                        put(Paths.get(DefaultClassesScanner.LIB_DIRECTORY, DEPENDENCY_JAR_NAME).toString(), dependencyFile);
                        put("uselessDirectory/", directory); //Just to test how directories are processed
                    }
                }
        );

        Files.delete(pluginJar);
        Files.delete(dependencyFile);
        Files.delete(libDirectory);

        return testPlugin;
    }

    public static Path[] createUnpackedTestPluginFile(Path directory) throws Exception {
        Path pluginJar = JarHelper.createJarFile(
                directory,
                DefaultClassesScanner.PLUGIN_CLASSES_FILE,
                Optional.empty(),
                new Class[]{PluginImpl.class, TestExtensionPointImpl.class},
                Collections.emptyMap()
        );
        Path libDirectory = directory.resolve(DefaultClassesScanner.LIB_DIRECTORY);
        Files.createDirectories(libDirectory);

        Path dependencyFile = JarHelper.createJarFile(
                libDirectory,
                DEPENDENCY_JAR_NAME,
                Optional.empty(),
                new Class[]{LibraryClass.class},
                Collections.emptyMap()
        );
        return new Path[]{pluginJar, libDirectory, dependencyFile};
    }

    public static Manifest createManifest(Map<String, String> customFields) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        for (String key : customFields.keySet()) {
            manifest.getMainAttributes().putValue(key, customFields.get(key));
        }
        return manifest;
    }
}
