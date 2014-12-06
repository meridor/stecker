package ru.meridor.stecker;

import org.junit.Test;
import ru.meridor.stecker.impl.FileSystemHelper;
import ru.meridor.stecker.impl.ManifestField;
import ru.meridor.stecker.impl.data.AnnotatedImpl;
import ru.meridor.stecker.impl.data.TestAnnotation;
import ru.meridor.stecker.impl.data.TestExtensionPoint;
import ru.meridor.stecker.impl.data.TestExtensionPointImpl;
import ru.meridor.stecker.interfaces.ClassesScanner;
import ru.meridor.stecker.interfaces.DependencyChecker;
import ru.meridor.stecker.interfaces.ManifestReader;
import ru.meridor.stecker.interfaces.ResourcesScanner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PluginLoaderTest {

    @Test
    public void testFluentApi() throws PluginException {
        Path pluginDirectory = Paths.get("plugin-directory");
        String fileGlob = "some-glob";
        Path cacheDirectory = Paths.get("cache-directory");
        Class[] extensionPointsArray = new Class[]{TestExtensionPoint.class, TestExtensionPoint.class}; //We intentionally duplicate extension points
        ManifestReader manifestReader = mock(ManifestReader.class);
        DependencyChecker dependencyChecker = mock(DependencyChecker.class);
        ClassesScanner classesScanner = mock(ClassesScanner.class);
        ResourcesScanner resourcesScanner = mock(ResourcesScanner.class);
        String[] resourcesGlobs = new String[]{"glob1", "glob2"};


        PluginLoader pluginLoader = PluginLoader
                .withPluginDirectory(pluginDirectory)
                .withFileGlob(fileGlob)
                .withCacheDirectory(cacheDirectory)
                .withExtensionPoints(extensionPointsArray)
                .withManifestReader(manifestReader)
                .withDependencyChecker(dependencyChecker)
                .withClassesScanner(classesScanner)
                .withResourcesScanner(resourcesScanner)
                .withResourcesPatterns(resourcesGlobs);

        assertThat(pluginLoader.getPluginDirectory(), equalTo(pluginDirectory));
        List<Class> uniqueExtensionPoints = Arrays.asList(extensionPointsArray)
                .stream().distinct()
                .collect(Collectors.toList());
        assertThat(pluginLoader.getExtensionPoints(), equalTo(uniqueExtensionPoints));
        assertThat(pluginLoader.getCacheDirectory(), equalTo(cacheDirectory));
        assertThat(pluginLoader.getFileGlob(), equalTo(fileGlob));
        assertThat(pluginLoader.getManifestReader(), equalTo(manifestReader));
        assertThat(pluginLoader.getDependencyChecker(), equalTo(dependencyChecker));
        assertThat(pluginLoader.getClassesScanner(), equalTo(classesScanner));
        assertThat(pluginLoader.getResourcesScanner(), equalTo(resourcesScanner));
        assertThat(pluginLoader.getResourcesPatterns(), equalTo(resourcesGlobs));
    }

    @Test(expected = PluginException.class)
    public void testIncorrectFileProvider() throws PluginException {
        PluginLoader.withPluginDirectory(null);
    }

    @Test
    public void testLoad() throws Exception {
        final String PLUGIN_NAME = "plugin-name";
        final String PLUGIN_VERSION = "plugin-version";
        Manifest manifest = createTestLoadManifest(PLUGIN_NAME, PLUGIN_VERSION);
        Path tempDirectory = FileSystemHelper.createTempDirectory(); //Can't use in-memory filesystems (e.g. Google JimFS) here because they don't support java.net.URL for class loader

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
                    .withExtensionPoints(TestExtensionPoint.class, TestAnnotation.class)
                    .withResourcesPatterns("glob:**/*.resource")
                    .load();

            assertThat(pluginRegistry.getPluginNames(), hasSize(1));
            assertThat(pluginRegistry.getPluginNames(), contains(PLUGIN_NAME));
            assertTrue(pluginRegistry.getPlugin(PLUGIN_NAME).isPresent());
            assertThat(pluginRegistry.getPlugin(PLUGIN_NAME).get().getVersion(), equalTo(PLUGIN_VERSION));

            assertThat(pluginRegistry.getExtensionPoints(), hasSize(2));
            assertThat(pluginRegistry.getExtensionPoints(), containsInAnyOrder(TestExtensionPoint.class, TestAnnotation.class));

            assertThat(pluginRegistry.getImplementations(TestAnnotation.class), hasSize(1));
            assertThat(pluginRegistry.getImplementations(TestAnnotation.class), contains(AnnotatedImpl.class));

            assertThat(pluginRegistry.getImplementations(TestExtensionPoint.class), hasSize(1));
            assertThat(pluginRegistry.getImplementations(TestExtensionPoint.class), contains(TestExtensionPointImpl.class));

            assertThat(pluginRegistry.getResources(), hasSize(1));
            assertThat(pluginRegistry.getResources("missing-plugin"), hasSize(0));
            assertThat(pluginRegistry.getResources(PLUGIN_NAME), hasSize(1));
            assertTrue(pluginRegistry.getResources(PLUGIN_NAME).get(0).endsWith(JarHelper.TEST_RESOURCE_NAME));
        } finally {
            FileSystemHelper.removeDirectory(tempDirectory);
        }
    }

    private Manifest createTestLoadManifest(String pluginName, String pluginVersion) {
        Map<String, String> manifestContents = new HashMap<String, String>() {
            {
                put(ManifestField.NAME.getFieldName(), pluginName);
                put(ManifestField.VERSION.getFieldName(), pluginVersion);
            }
        };
        return JarHelper.createManifest(manifestContents);
    }
}
