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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
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
        Map<Class, List<Class>> classesMap = devClassesScanner.scan(baseDirectory, EXTENSION_POINTS);

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

    @Test
    public void testPluginDirectoryMissing() throws PluginException {
        DevClassesScanner devClassesScanner = new DevClassesScanner(BuildToolType.MAVEN);
        Map<Class, List<Class>> classesMap = devClassesScanner.scan(Paths.get("missing-directory"), EXTENSION_POINTS);
        assertThat(classesMap.keySet(), hasSize(0));
    }

    @Test
    public void testClassesDirectoryMissing() throws Exception {
        createBaseDirectory();
        DevClassesScanner devClassesScanner = new DevClassesScanner(BuildToolType.MAVEN);
        Map<Class, List<Class>> classesMap = devClassesScanner.scan(baseDirectory, EXTENSION_POINTS);
        assertThat(classesMap.keySet(), hasSize(0));
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