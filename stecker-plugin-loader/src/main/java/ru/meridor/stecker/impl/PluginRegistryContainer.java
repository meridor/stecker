package ru.meridor.stecker.impl;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.PluginMetadata;
import ru.meridor.stecker.PluginRegistry;
import ru.meridor.stecker.interfaces.Dependency;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PluginRegistryContainer implements PluginRegistry {

    private final Map<Class, List<Class>> registry = new HashMap<>();

    private final Map<String, PluginMetadata> plugins = new HashMap<>();

    private final Map<String, List<Path>> resources = new HashMap<>();

    @Override
    public void addImplementations(Class extensionPoint, List<Class> implementationClasses) {
        if (!registry.containsKey(extensionPoint)) {
            registry.put(extensionPoint, new ArrayList<>());
        }
        registry.get(extensionPoint).addAll(implementationClasses);
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
        return new ArrayList<String>() {
            {
                addAll(plugins.keySet());
            }
        };
    }

    @Override
    public List<Class> getExtensionPoints() {
        return new ArrayList<Class>() {
            {
                addAll(registry.keySet());
            }
        };
    }

    @Override
    public List<Class> getImplementations(Class extensionPoint) {
        return registry.containsKey(extensionPoint) ?
                registry.get(extensionPoint) : Collections.emptyList();
    }

    @Override
    public void addResources(String pluginName, List<Path> resourcesList) {
        resources.put(pluginName, resourcesList);
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
}
