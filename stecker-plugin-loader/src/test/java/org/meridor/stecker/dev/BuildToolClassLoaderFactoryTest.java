package org.meridor.stecker.dev;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class BuildToolClassLoaderFactoryTest {

    private static final Path BASE_DIRECTORY = Paths.get("some-directory");

    private static final Path TARGET_CLASSES_DIRECTORY = BASE_DIRECTORY.resolve("target").resolve("classes");

    private static final Path TARGET_LIB_DIRECTORY = BASE_DIRECTORY.resolve("target").resolve("plugin-generator").resolve("data").resolve("lib");

    @Parameterized.Parameters(name = "{0}")
    public static Collection parameters() throws MalformedURLException {
        return Arrays.asList(new Object[][]{
                {
                        BuildToolType.MAVEN,
                        TARGET_CLASSES_DIRECTORY,
                        TARGET_LIB_DIRECTORY
                }
        });
    }

    private final BuildToolType buildToolType;

    private final Path correctClassesPath;

    private final Path correctDependenciesPath;

    public BuildToolClassLoaderFactoryTest(BuildToolType buildToolType, Path correctClassesPath, Path correctDependenciesPath) {
        this.buildToolType = buildToolType;
        this.correctClassesPath = correctClassesPath;
        this.correctDependenciesPath = correctDependenciesPath;
    }

    @Test
    public void testGetClassesPath() throws Exception {
        assertThat(BuildToolClassLoaderFactory.getClassesPath(BASE_DIRECTORY, buildToolType), equalTo(correctClassesPath));
    }

    @Test
    public void testGetDependenciesPath() throws Exception {
        assertThat(BuildToolClassLoaderFactory.getDependenciesPath(BASE_DIRECTORY, buildToolType), equalTo(correctDependenciesPath));
    }

}