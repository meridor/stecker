package org.meridor.stecker.interfaces;


import org.meridor.stecker.PluginException;

import java.nio.file.Path;
import java.util.List;

/**
 * Scans specified path for resources and returns a list of matching resources
 */
public interface ResourcesScanner {

    /**
     * Does the scanning
     *
     * @param path depending on context can be a file or directory path
     * @return a list of matching resources
     * @throws PluginException
     */
    List<Path> scan(Path path) throws PluginException;

}
