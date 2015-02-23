package org.meridor.stecker.interfaces;

import java.util.Optional;

public interface ClassLoaderAware {

    /**
     * Returns class loader instance for plugin specified
     *
     * @param pluginName plugin name to process
     * @return class loader instance or empty
     */
    Optional<ClassLoader> getClassLoader(String pluginName);

}
