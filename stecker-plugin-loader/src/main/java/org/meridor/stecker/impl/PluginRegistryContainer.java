package org.meridor.stecker.impl;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;
import org.meridor.stecker.PluginRegistry;
import org.meridor.stecker.interfaces.Dependency;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginRegistryContainer implements PluginRegistry {

    private final Map<String, ClassesRegistry> registry = new HashMap<>();

    private final Map<String, PluginMetadata> plugins = new HashMap<>();

    private final Map<String, List<Path>> resources = new HashMap<>();

    @Override
    public void addImplementations(PluginMetadata pluginMetadata, Class extensionPoint, List<Class> implementationClasses) {
        if (!registry.containsKey(pluginMetadata.getName())) {
            registry.put(pluginMetadata.getName(), new ClassesRegistry(extensionPoint, implementationClasses));
        } else {
            registry.get(pluginMetadata.getName()).put(extensionPoint, implementationClasses);
        }
    }

    @Override
    public void addPlugin(PluginMetadata pluginMetadata) throws PluginException {
        plugins.put(pluginMetadata.getName(), pluginMetadata);
        Optional<Dependency> providedDependency = pluginMetadata.getProvidedDependency();
        if (providedDependency.isPresent()) {
            Optional<PluginMetadata> anotherPlugin = getPlugin(providedDependency.get().getName());
            if (anotherPlugin.isPresent()) {
                throw new PluginException("Another plugin providing the same virtual dependency found: " + anotherPlugin.get().getName())
                        .withDependencyProblem(new DependencyProblemContainer(
                                Collections.emptyList(),
                                new ArrayList<Dependency>() {
                                    {
                                        add(anotherPlugin.get().getDependency());
                                    }
                                }
                        ));
            }
            plugins.put(providedDependency.get().getName(), pluginMetadata);
        }
    }

    @Override
    public Optional<PluginMetadata> getPlugin(String pluginName) {
        return Optional.ofNullable(plugins.get(pluginName));
    }

    @Override
    public List<String> getPluginNames() {
        return new ArrayList<>(plugins.keySet());
    }

    @Override
    public List<Class> getExtensionPoints() {
        return registry.values().stream()
                .flatMap(cr -> cr.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Class> getImplementations(Class extensionPoint) {
        return registry.values().stream()
                .flatMap(cr -> cr.getImplementations(extensionPoint).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Class> getExtensionPoints(String pluginName) {
        return registry.containsKey(pluginName) ? 
                new ArrayList<>(registry.get(pluginName).keySet()) :
                Collections.emptyList();
    }

    @Override
    public List<Class> getImplementations(String pluginName, Class extensionPoint) {
        return registry.containsKey(pluginName) ?
                registry.get(pluginName).getImplementations(extensionPoint) :
                Collections.emptyList();
    }

    @Override
    public void addResources(PluginMetadata pluginMetadata, List<Path> resourcesList) {
        resources.put(pluginMetadata.getName(), resourcesList);
    }

    @Override
    public List<Path> getResources(String pluginName) {
        return resources.containsKey(pluginName) ?
                resources.get(pluginName) :
                Collections.emptyList();
    }

    @Override
    public List<Path> getResources() {
        return resources.values().stream().reduce(new ArrayList<>(), (paths, paths2) -> {
            paths.addAll(paths2);
            return paths;
        });
    }
    
    private static class ClassesRegistry extends HashMap<Class, List<Class>> {
        
        public ClassesRegistry(Class extensionPoint, List<Class> implementationClasses) {
            if (!containsKey(extensionPoint)) {
                put(extensionPoint, new ArrayList<>());
            }
            get(extensionPoint).addAll(implementationClasses);
        }
        
        public List<Class> getImplementations(Class extensionPoint) {
            return containsKey(extensionPoint) ?
                    get(extensionPoint) : Collections.emptyList();
        }
        
    }
    
}
