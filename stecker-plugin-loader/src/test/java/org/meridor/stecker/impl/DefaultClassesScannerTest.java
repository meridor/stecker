package org.meridor.stecker.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.meridor.stecker.JarHelper;
import org.meridor.stecker.PluginException;
import org.meridor.stecker.impl.data.AnnotatedImpl;
import org.meridor.stecker.impl.data.TestAnnotation;
import org.meridor.stecker.impl.data.TestExtensionPoint;
import org.meridor.stecker.impl.data.TestExtensionPointImpl;
import org.meridor.stecker.interfaces.PluginImplementationsAware;
import org.meridor.stecker.interfaces.ScanResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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

        ScanResult scanResult = new DefaultClassesScanner(cacheDirectory).scan(pluginFile, extensionPoints);

        assertThat(scanResult.getClassLoader(), notNullValue());

        PluginImplementationsAware contents = scanResult.getContents();
        assertThat(contents.getExtensionPoints(), hasSize(2));

        List<Class> pluginImplementations = contents.getImplementations(TestAnnotation.class);
        assertThat(pluginImplementations, hasSize(1));
        assertThat(pluginImplementations, contains(AnnotatedImpl.class));

        List<Class> testExtensionPointImplementations = contents.getImplementations(TestExtensionPoint.class);
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
