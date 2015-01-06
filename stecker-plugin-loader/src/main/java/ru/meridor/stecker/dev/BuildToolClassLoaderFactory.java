package ru.meridor.stecker.dev;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.impl.PluginUtils;

import java.nio.file.Path;

public class BuildToolClassLoaderFactory {

    public static ClassLoader getClassLoader(Path baseDirectory, BuildToolType buildToolType) throws PluginException {
        switch (buildToolType) {
            default:
            case MAVEN: {
                return PluginUtils.getClassLoader(
                        getClassesPath(baseDirectory, buildToolType),
                        getDependenciesPath(baseDirectory, buildToolType)
                );
            }
        }
    }

    public static Path getClassesPath(Path baseDirectory, BuildToolType buildToolType) {
        switch (buildToolType) {
            default:
            case MAVEN:
                return getTargetDirectory(baseDirectory).resolve("classes");
        }
    }

    public static Path getDependenciesPath(Path baseDirectory, BuildToolType buildToolType) {
        switch (buildToolType) {
            default:
            case MAVEN:
                return getTargetDirectory(baseDirectory).resolve("plugin-generator").resolve("data").resolve("lib");
        }
    }

    private static Path getTargetDirectory(Path baseDirectory) {
        return baseDirectory.resolve("target");
    }

}
