package org.meridor.stecker.interfaces;

import org.meridor.stecker.PluginException;

import java.nio.file.Path;
import java.util.List;

/**
 * Returns a list of {@link Class}es corresponding to specified extension points
 */
public interface ClassesScanner {

    /**
     * Returns a mapping between extension point classes and plugin file classes. E.g. if extension point is
     * an interface <b>A</b> and some plugin implements it in classes B and C then the mapping will be: A -&gt; (B, C).
     *
     * @param pluginFile      plugin file to process
     * @param extensionPoints a list of extension point classes
     * @return mapping from extension point to implementations from a plugin
     * @throws org.meridor.stecker.PluginException when something goes wrong during classes scanning
     */
    ScanResult scan(Path pluginFile, List<Class> extensionPoints) throws PluginException;

}
