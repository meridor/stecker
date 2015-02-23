package org.meridor.stecker.impl;

import org.meridor.stecker.interfaces.PluginImplementationsAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassesRegistry implements PluginImplementationsAware {

    private final Map<Class, List<Class>> contents = new HashMap<>();

    public ClassesRegistry(Map<Class, List<Class>> contents) {
        for (Class extensionPoint : contents.keySet()) {
            addImplementations(extensionPoint, contents.get(extensionPoint));
        }
    }

    public ClassesRegistry(Class extensionPoint, List<Class> implementationClasses) {
        addImplementations(extensionPoint, implementationClasses);
    }

    public List<Class> getImplementations(Class extensionPoint) {
        return contents.containsKey(extensionPoint) ?
                contents.get(extensionPoint) : Collections.emptyList();
    }

    public void addImplementations(Class extensionPoint, List<Class> implementationClasses) {
        if (!contents.containsKey(extensionPoint)) {
            contents.put(extensionPoint, new ArrayList<>());
        }
        contents.get(extensionPoint).addAll(implementationClasses);
    }

    public List<Class> getExtensionPoints() {
        return new ArrayList<>(contents.keySet());
    }

}
