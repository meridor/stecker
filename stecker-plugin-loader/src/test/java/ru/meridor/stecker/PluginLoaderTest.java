package ru.meridor.stecker;

import org.junit.Rule;
import org.junit.Test;
import ru.meridor.stecker.impl.ManifestField;
import ru.meridor.stecker.impl.data.AnnotatedImpl;
import ru.meridor.stecker.impl.data.TestAnnotation;
import ru.meridor.stecker.impl.data.TestExtensionPoint;
import ru.meridor.stecker.impl.data.TestExtensionPointImpl;
import ru.meridor.stecker.interfaces.ClassesScanner;
import ru.meridor.stecker.interfaces.DependencyChecker;
import ru.meridor.stecker.interfaces.ManifestReader;
import ru.meridor.stecker.interfaces.PluginsProvider;
import ru.meridor.stecker.interfaces.ResourcesScanner;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PluginLoaderTest {

    @Rule
    public TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

    @Test
    public void testFluentApi() throws PluginException {
        Path pluginDirectory = Paths.get("plugin-directory");
        String fileGlob = "some-glob";
        PluginsProvider pluginsProvider = mock(PluginsProvider.class);
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
                .withPluginsProvider(pluginsProvider)
                .withCacheDirectory(cacheDirectory)
                .withExtensionPoints(extensionPointsArray)
                .withManifestReader(manifestReader)
                .withDependencyChecker(dependencyChecker)
                .withClassesScanner(classesScanner)
                .withResourcesScanner(resourcesScanner)
                .withResourcesPatterns(resourcesGlobs);

        assertThat(pluginLoader.getPluginsDirectory(), equalTo(pluginDirectory));
        List<Class> uniqueExtensionPoints = Arrays.asList(extensionPointsArray)
                .stream().distinct()
                .collect(Collectors.toList());
        assertThat(pluginLoader.getExtensionPoints(), equalTo(uniqueExtensionPoints));
        assertThat(pluginLoader.getCacheDirectory(), equalTo(cacheDirectory));
        assertThat(pluginLoader.getFileGlob(), equalTo(fileGlob));
        assertThat(pluginLoader.getPluginsProvider(), equalTo(pluginsProvider));
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
        Path tempDirectory = temporaryDirectory.getDirectory(); //Can't use in-memory filesystems (e.g. Google JimFS) here because they don't support java.net.URL for class loader

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
        Path resourcePath = pluginRegistry.getResources(PLUGIN_NAME).get(0);
        assertTrue(resourcePath.endsWith(JarHelper.TEST_RESOURCE_NAME));

        try (InputStream inputStream = Files.newInputStream(resourcePath)) {
            assertNotNull(inputStream); //We should be able to open resource
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
