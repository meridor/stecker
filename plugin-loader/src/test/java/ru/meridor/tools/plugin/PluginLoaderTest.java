package ru.meridor.tools.plugin;

import org.junit.Test;
import ru.meridor.tools.plugin.impl.*;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.jar.Manifest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PluginLoaderTest {

    @Test
    public void testFluentApi() throws PluginException {
        FileProvider fileProvider = mock(FileProvider.class);
        Class[] extensionPoints = new Class[]{TestExtensionPoint.class, TestExtensionPoint.class}; //We explicitly duplicate extension points
        FileFilter fileFilter = mock(FileFilter.class);
        ManifestReader manifestReader = mock(ManifestReader.class);
        DependencyChecker dependencyChecker = mock(DependencyChecker.class);
        ClassesScanner classesScanner = mock(ClassesScanner.class);
        ClassLoader classLoader = mock(ClassLoader.class);
        PluginLoader pluginLoader = PluginLoader
                .withFileProvider(fileProvider)
                .withExtensionPoints(extensionPoints)
                .withFileFilter(fileFilter)
                .withManifestReader(manifestReader)
                .withDependencyChecker(dependencyChecker)
                .withClassesScanner(classesScanner)
                .withClassLoader(classLoader);
        assertEquals(fileProvider, pluginLoader.getFileProvider());
        assertEquals(new ArrayList<Class>(new HashSet<>(Arrays.asList(extensionPoints))), pluginLoader.getExtensionPoints());
        assertEquals(fileFilter, pluginLoader.getFileFilter());
        assertEquals(manifestReader, pluginLoader.getManifestReader());
        assertEquals(dependencyChecker, pluginLoader.getDependencyChecker());
        assertEquals(classesScanner, pluginLoader.getClassesScanner());
        assertEquals(classLoader, pluginLoader.getClassLoader());
    }

    @Test(expected = PluginException.class)
    public void testIncorrectFileProvider() throws PluginException {
        PluginLoader.withFileProvider(null);
    }

    //TODO: migrate to in-memory file system!
    @Test
    public void testLoad() throws Exception {
        final String PLUGIN_NAME = "plugin-name";
        final String PLUGIN_VERSION = "plugin-version";
        Map<String, String> manifestContents = new HashMap<String, String>(){
            {
                put(ManifestField.NAME.getFieldName(), PLUGIN_NAME);
                put(ManifestField.VERSION.getFieldName(), PLUGIN_VERSION);
            }
        };
        Manifest manifest = JarHelper.createManifest(manifestContents);
        File tempDirectory = null;
        try {
            tempDirectory = Files.createTempDirectory("plugin-loader", new FileAttribute[0]).toFile();
            assertNotNull(tempDirectory);
            JarHelper.createJarFile(
                    tempDirectory,
                    "test-plugin.jar",
                    manifest,
                    TestExtensionPointImpl.class, PluginImpl.class);

            PluginRegistry pluginRegistry = PluginLoader
                    .withFileProvider(new DefaultFileProvider(tempDirectory))
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
            if (tempDirectory != null && tempDirectory.exists() && tempDirectory.isDirectory()) {
                File[] files = tempDirectory.listFiles();
                if (files != null) {
                    for (File file : files){
                        assertTrue(file.delete());
                    }
                }
                assertTrue(tempDirectory.delete());
            }
        }
    }
}
