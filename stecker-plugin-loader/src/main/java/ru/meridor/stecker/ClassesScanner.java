package ru.meridor.stecker;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Returns a list of {@link Class}es corresponding to specified extension points
 */
public interface ClassesScanner {

    /**
     * Returns a mapping between extension point classes and plugin file classes. E.g. if extension point is
     * an interface <b>A</b> and some plugin implements it in classes B and C then the mapping will be: A -> (B, C).
     *
     * @param pluginFile      plugin file to process
     * @param extensionPoints a list of extension point classes
     * @return mapping from extension point to implementations from a plugin
     * @throws PluginException
     */
    Map<Class, List<Class>> scan(Path pluginFile, List<Class> extensionPoints) throws PluginException;

}
