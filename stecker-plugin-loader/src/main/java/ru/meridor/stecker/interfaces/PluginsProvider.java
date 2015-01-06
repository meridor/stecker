package ru.meridor.stecker.interfaces;

import ru.meridor.stecker.PluginException;

import java.nio.file.Path;
import java.util.List;

/**
 * Provides a list of paths to be considered as plugins
 */
public interface PluginsProvider {

    /**
     * Provides a list of plugins paths
     *
     * @param baseDirectory root directory to scan for plugins
     * @return list of paths that seems to be plugins
     * @throws PluginException
     */
    List<Path> provide(Path baseDirectory) throws PluginException;

}
