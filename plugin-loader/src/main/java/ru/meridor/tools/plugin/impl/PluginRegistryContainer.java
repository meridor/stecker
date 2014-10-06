package ru.meridor.tools.plugin.impl;

import ru.meridor.tools.plugin.Dependency;
import ru.meridor.tools.plugin.PluginException;
import ru.meridor.tools.plugin.PluginMetadata;
import ru.meridor.tools.plugin.PluginRegistry;

import java.util.*;

public class PluginRegistryContainer implements PluginRegistry {

    private Map<Class, List<Class>> registry = new HashMap<>();

    private Map<String, PluginMetadata> plugins = new HashMap<>();

    @Override
    public void addImplementations(Class extensionPoint, List<Class> implementationClasses) {
        if (!registry.containsKey(extensionPoint)){
            registry.put(extensionPoint, new ArrayList<>());
        }
        registry.get(extensionPoint).addAll(implementationClasses);
    }

    @Override
    public void addPlugin(PluginMetadata pluginMetadata) throws PluginException {
        plugins.put(pluginMetadata.getName(), pluginMetadata);
        Optional<Dependency> providedDependency = pluginMetadata.getProvidedDependency();
        if (providedDependency.isPresent()){
            Optional<PluginMetadata> anotherPlugin = getPlugin(providedDependency.get().getName());
            if (anotherPlugin.isPresent()){
                throw new PluginException("Another plugin providing the same virtual dependency found: " + anotherPlugin.get().getName())
                        .withDependencyProblem(new DependencyProblemContainer(
                                Collections.emptyList(),
                                new ArrayList<Dependency>(){
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
        return new ArrayList<String>(){
            {
                addAll(plugins.keySet());
            }
        };
    }

    @Override
    public List<Class> getExtensionPoints() {
        return new ArrayList<Class>(){
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

}
