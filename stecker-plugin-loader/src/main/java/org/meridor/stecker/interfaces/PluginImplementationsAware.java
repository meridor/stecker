package org.meridor.stecker.interfaces;

import java.util.List;

/**
 * Knows about extension points and implementations corresponding to them
 */
public interface PluginImplementationsAware {

    /**
     * Returns a list of present extension points
     *
     * @return a list of extension points
     */
    List<Class> getExtensionPoints();

    /**
     * Returns classes implementing extension point
     *
     * @param extensionPoint extension point class
     * @return a list of implementation classes
     */
    List<Class> getImplementations(Class extensionPoint);

}
