package ru.meridor.stecker.interfaces;

import java.util.List;

public interface ImplementationsAware {

    /**
     * Returns a list of valid extension points
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
