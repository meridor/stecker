package ru.meridor.tools.plugin;

import org.junit.Test;
import ru.meridor.tools.plugin.impl.FileSystemHelper;
import ru.meridor.tools.plugin.impl.ManifestField;
import ru.meridor.tools.plugin.impl.data.PluginImpl;
import ru.meridor.tools.plugin.impl.data.TestExtensionPoint;
import ru.meridor.tools.plugin.impl.data.TestExtensionPointImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Manifest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PluginLoaderTest {

    @Test
    public void testFluentApi() throws PluginException {
        Path pluginDirectory = Paths.get("plugin-directory");
        String fileGlob = "some-glob";
        Path cacheDirectory = Paths.get("cache-directory");
        Class[] extensionPoints = new Class[]{TestExtensionPoint.class, TestExtensionPoint.class}; //We explicitly duplicate extension points
        ManifestReader manifestReader = mock(ManifestReader.class);
        DependencyChecker dependencyChecker = mock(DependencyChecker.class);
        ClassesScanner classesScanner = mock(ClassesScanner.class);
        PluginLoader pluginLoader = PluginLoader
                .withPluginDirectory(pluginDirectory)
                .withFileGlob(fileGlob)
                .withCacheDirectory(cacheDirectory)
                .withExtensionPoints(extensionPoints)
                .withManifestReader(manifestReader)
                .withDependencyChecker(dependencyChecker)
                .withClassesScanner(classesScanner);
        assertEquals(pluginDirectory, pluginLoader.getPluginDirectory());
        List<Class> extensionPointsList = new ArrayList<Class>(new HashSet<>(Arrays.asList(extensionPoints)));
        assertEquals(extensionPointsList, pluginLoader.getExtensionPoints());
        assertEquals(cacheDirectory, pluginLoader.getCacheDirectory());
        assertEquals(fileGlob, pluginLoader.getFileGlob());
        assertEquals(manifestReader, pluginLoader.getManifestReader());
        assertEquals(dependencyChecker, pluginLoader.getDependencyChecker());
        assertEquals(classesScanner, pluginLoader.getClassesScanner());
    }

    @Test(expected = PluginException.class)
    public void testIncorrectFileProvider() throws PluginException {
        PluginLoader.withPluginDirectory(null);
    }

    @Test
    public void testLoad() throws Exception {
        final String PLUGIN_NAME = "plugin-name";
        final String PLUGIN_VERSION = "plugin-version";
        Map<String, String> manifestContents = new HashMap<String, String>() {
            {
                put(ManifestField.NAME.getFieldName(), PLUGIN_NAME);
                put(ManifestField.VERSION.getFieldName(), PLUGIN_VERSION);
            }
        };
        Manifest manifest = JarHelper.createManifest(manifestContents);
        //Can't use in-memory filesystems (e.g. Google JimFS) here because they don't support java.net.URL for class loader
        Path tempDirectory = FileSystemHelper.createTempDirectory();
        try {
            assertNotNull(tempDirectory);
            assertTrue(Files.exists(tempDirectory));
            JarHelper.createTestPluginFile(
                    "some-plugin",
                    tempDirectory,
                    Optional.of(manifest)
            );

            PluginRegistry pluginRegistry = PluginLoader
                    .withPluginDirectory(tempDirectory)
                    .withExtensionPoints(TestExtensionPoint.class)
                    .load();

            assertEquals(1, pluginRegistry.getPluginNames().size());
            assertEquals(PLUGIN_NAME, pluginRegistry.getPluginNames().get(0));
            assertTrue(pluginRegistry.getPlugin(PLUGIN_NAME).isPresent());
            assertEquals(PLUGIN_VERSION, pluginRegistry.getPlugin(PLUGIN_NAME).get().getVersion());
            assertEquals(2, pluginRegistry.getExtensionPoints().size());
            assertTrue(pluginRegistry.getExtensionPoints().contains(Plugin.class));
            assertEquals(1, pluginRegistry.getImplementations(Plugin.class).size());
            assertTrue(pluginRegistry.getImplementations(Plugin.class).contains(PluginImpl.class));
            assertTrue(pluginRegistry.getExtensionPoints().contains(TestExtensionPoint.class));
            assertEquals(1, pluginRegistry.getImplementations(TestExtensionPoint.class).size());
            assertTrue(pluginRegistry.getImplementations(TestExtensionPoint.class).contains(TestExtensionPointImpl.class));
        } finally {
            FileSystemHelper.removeDirectory(tempDirectory);
        }
    }
}
