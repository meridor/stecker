package org.meridor.stecker.dev;

import org.junit.Rule;
import org.junit.Test;
import org.meridor.stecker.JarHelper;
import org.meridor.stecker.PluginException;
import org.meridor.stecker.TemporaryDirectory;
import org.meridor.stecker.impl.data.AnnotatedImpl;
import org.meridor.stecker.impl.data.TestAnnotation;
import org.meridor.stecker.impl.data.TestExtensionPoint;
import org.meridor.stecker.impl.data.TestExtensionPointImpl;
import org.meridor.stecker.interfaces.PluginImplementationsAware;
import org.meridor.stecker.interfaces.ScanResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DevClassesScannerTest {

    @Rule
    public TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

    private static final List<Class> EXTENSION_POINTS = Arrays.asList(new Class[]{
            TestExtensionPoint.class,
            TestAnnotation.class
    });

    private Path baseDirectory;

    @Test
    public void testScan() throws Exception {

        createFiles();

        DevClassesScanner devClassesScanner = new DevClassesScanner(BuildToolType.MAVEN);
        ScanResult scanResult = devClassesScanner.scan(baseDirectory, EXTENSION_POINTS);

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

    @Test
    public void testPluginDirectoryMissing() throws PluginException {
        DevClassesScanner devClassesScanner = new DevClassesScanner(BuildToolType.MAVEN);
        ScanResult scanResult = devClassesScanner.scan(Paths.get("missing-directory"), EXTENSION_POINTS);
        assertThat(scanResult.getContents().getExtensionPoints(), hasSize(0));
    }

    @Test
    public void testClassesDirectoryMissing() throws Exception {
        createBaseDirectory();
        DevClassesScanner devClassesScanner = new DevClassesScanner(BuildToolType.MAVEN);
        ScanResult scanResult = devClassesScanner.scan(baseDirectory, EXTENSION_POINTS);
        assertThat(scanResult.getContents().getExtensionPoints(), hasSize(0));
    }

    private void createBaseDirectory() throws IOException {
        baseDirectory = temporaryDirectory.getDirectory();
        Files.createDirectories(baseDirectory);
    }

    private void createFiles() throws Exception {
        createBaseDirectory();
        Path classesDirectory = baseDirectory.resolve("target").resolve("classes");
        Files.createDirectories(classesDirectory);
        Class[] implementations = new Class[]{
                TestExtensionPointImpl.class,
                AnnotatedImpl.class
        };
        for (Class aClass : implementations) {
            String resourceName = JarHelper.classToResourceName(aClass);
            Path classSourcePath = JarHelper.classToPath(aClass);
            Path classDestinationPath = classesDirectory.resolve(resourceName);
            Files.createDirectories(classDestinationPath.getParent());
            Files.copy(classSourcePath, classDestinationPath);
        }
    }
}