package ru.meridor.stecker.dev;

import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.PluginMetadata;
import ru.meridor.stecker.interfaces.DependencyChecker;
import ru.meridor.stecker.interfaces.PluginsAware;

public class DevDependencyChecker implements DependencyChecker {
    @Override
    public void check(PluginsAware pluginRegistry, PluginMetadata pluginMetadata) throws PluginException {
        //Just ignores dependency checks
    }
}
