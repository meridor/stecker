package org.meridor.stecker.interfaces;

import java.util.List;

public interface ImplementationsAware {

    /**
     * Returns a list of valid extension points across all plugins
     *
     * @return a list of extension points
     */
    List<Class> getExtensionPoints();

    /**
     * Returns a list of extension points having implementations in current plugin
     * @param pluginName name of plugin to process
     * @return a list of extension points
     */
    List<Class> getExtensionPoints(String pluginName);
    
    /**
     * Returns classes implementing extension point
     *
     * @param extensionPoint extension point class
     * @return a list of implementation classes
     */
    List<Class> getImplementations(Class extensionPoint);
    
    /**
     * Returns classes implementing extension point
     *
     * @param pluginName name of plugin to process
     * @param extensionPoint extension point class
     * @return a list of implementation classes
     */
    List<Class> getImplementations(String pluginName, Class extensionPoint);

}
