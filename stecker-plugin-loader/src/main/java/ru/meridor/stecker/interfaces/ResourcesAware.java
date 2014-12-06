package ru.meridor.stecker.interfaces;

import java.nio.file.Path;
import java.util.List;

public interface ResourcesAware {

    /**
     * Returns resources for specific plugin
     *
     * @param pluginName plugin name
     * @return a list of resources
     */
    List<Path> getResources(String pluginName);

    /**
     * Returns all resources
     *
     * @return a list of resources
     */
    List<Path> getResources();

}
