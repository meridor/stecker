package ru.meridor.stecker.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.meridor.stecker.JarHelper;
import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.impl.data.AnnotatedImpl;
import ru.meridor.stecker.impl.data.TestAnnotation;
import ru.meridor.stecker.impl.data.TestExtensionPoint;
import ru.meridor.stecker.impl.data.TestExtensionPointImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class DefaultClassesScannerTest {

    private static final String PLUGIN_NAME = "some-plugin";

    private Path tempDirectory;

    @Before
    public void createTempDirectory() throws IOException {
        tempDirectory = FileSystemHelper.createTempDirectory();
    }

    private Path getCacheDirectory() {
        return tempDirectory.resolve(".cache");
    }

    private Path getPluginCachePath() {
        return getCacheDirectory().resolve(PLUGIN_NAME);
    }

    private Path getPluginFilePath() {
        return tempDirectory.resolve(PLUGIN_NAME + ".jar");
    }

    @Test
    public void testUnpackPluginAndScan() throws Exception {
        Path cacheDirectory = getCacheDirectory();
        Files.createDirectories(getPluginCachePath());
        Path pluginFile = JarHelper.createTestPluginFile(PLUGIN_NAME, tempDirectory, Optional.empty());

        testScan(cacheDirectory, pluginFile);
    }

    @Test
    public void testUsePluginCacheAndScan() throws Exception {
        Path cacheDirectory = getCacheDirectory();

        Path pluginFile = JarHelper.createTestPluginFile(PLUGIN_NAME, tempDirectory, Optional.empty());
        Thread.sleep(1000); //We create plugin cache after plugin was created and thus cache should be used. Time precision is 1 second.
        PluginUtils.unpackPlugin(pluginFile, cacheDirectory);

        testScan(cacheDirectory, pluginFile);
    }

    private void testScan(Path cacheDirectory, Path pluginFile) throws PluginException {
        List<Class> extensionPoints = new ArrayList<Class>() {
            {
                add(TestExtensionPoint.class);
                add(TestAnnotation.class);
            }
        };

        Map<Class, List<Class>> classesMap = new DefaultClassesScanner(cacheDirectory).scan(pluginFile, extensionPoints);

        assertThat(classesMap.entrySet(), hasSize(2));

        assertThat(classesMap, hasKey(TestAnnotation.class));
        List<Class> pluginImplementations = classesMap.get(TestAnnotation.class);
        assertThat(pluginImplementations, hasSize(1));
        assertThat(pluginImplementations, contains(AnnotatedImpl.class));

        assertThat(classesMap, hasKey(TestExtensionPoint.class));
        List<Class> testExtensionPointImplementations = classesMap.get(TestExtensionPoint.class);
        assertThat(testExtensionPointImplementations, hasSize(1));
        assertThat(testExtensionPointImplementations, contains(TestExtensionPointImpl.class));

    }

    @Test(expected = PluginException.class)
    public void testInvalidPluginCacheDirectory() throws PluginException, IOException {
        Path cacheDirectory = getCacheDirectory();
        Path pluginCacheDirectory = getPluginCachePath();
        Files.createDirectories(cacheDirectory);
        Files.createFile(pluginCacheDirectory); //We create a file instead of directory

        Path pluginFile = getPluginFilePath();

        new DefaultClassesScanner(cacheDirectory).scan(pluginFile, new ArrayList<>());
    }

    @After
    public void removeTempDirectory() throws IOException {
        FileSystemHelper.removeDirectory(tempDirectory);
    }

}
