package ru.meridor.tools.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Returns a list of {@link Class}es corresponding to specified extension points
 */
public interface ClassesScanner {

    /**
     * Returns a mapping between extension point classes and plugin file classes. E.g. if extension point is
     * an interface <b>A</b> and some plugin implements it in classes B and C then the mapping will be: A -> (B, C).
     * @param extensionPoints a list extension point classes
     * @param pluginFile plugin file to process
     * @return mapping from extension point to implementations from a plugin
     * @throws PluginException
     */
    Map<Class, List<Class>> scan(List<Class> extensionPoints, File pluginFile) throws PluginException;

    /**
     * Returns class loader used to load class files
     * @return class loader instance
     */
    ClassLoader getClassLoader();

}
