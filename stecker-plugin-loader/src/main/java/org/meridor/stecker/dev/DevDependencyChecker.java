package org.meridor.stecker.dev;

import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;
import org.meridor.stecker.interfaces.DependencyChecker;
import org.meridor.stecker.interfaces.PluginsAware;

public class DevDependencyChecker implements DependencyChecker {
    @Override
    public void check(PluginsAware pluginRegistry, PluginMetadata pluginMetadata) throws PluginException {
        //Just ignores dependency checks
    }
}
