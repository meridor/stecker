package org.meridor.stecker.impl;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;
import org.meridor.stecker.PluginRegistry;
import org.meridor.stecker.interfaces.Dependency;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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

    private final Map<String, ClassLoader> classLoaders = new HashMap<>();

    @Override
    public void addImplementations(PluginMetadata pluginMetadata, Class extensionPoint, List<Class> implementationClasses) {
        if (!registry.containsKey(pluginMetadata.getName())) {
            registry.put(pluginMetadata.getName(), new ClassesRegistry(extensionPoint, implementationClasses));
        } else {
            registry.get(pluginMetadata.getName()).addImplementations(extensionPoint, implementationClasses);
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
                .flatMap(cr -> cr.getExtensionPoints().stream())
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
                new ArrayList<>(registry.get(pluginName).getExtensionPoints()) :
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
        return resources.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public void addClassLoader(PluginMetadata pluginMetadata, ClassLoader classLoader) {
        classLoaders.put(pluginMetadata.getName(), classLoader);
    }

    @Override
    public Optional<ClassLoader> getClassLoader(String pluginName) {
        return Optional.ofNullable(classLoaders.get(pluginName));
    }
}
