package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import ru.meridor.tools.plugin.Dependency;
import ru.meridor.tools.plugin.PluginException;
import ru.meridor.tools.plugin.PluginMetadata;
import ru.meridor.tools.plugin.PluginRegistry;
import ru.meridor.tools.plugin.impl.data.TestExtensionPoint;
import ru.meridor.tools.plugin.impl.data.TestExtensionPointImpl;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginRegistryContainerTest {

    private static final String PLUGIN_NAME = "test-plugin";

    @Test
    public void testPluginsRegistry() throws PluginException {
        PluginRegistry pluginRegistry = new PluginRegistryContainer();
        PluginMetadata pluginMetadata = mock(PluginMetadata.class);
        when(pluginMetadata.getName()).thenReturn(PLUGIN_NAME);
        when(pluginMetadata.getProvidedDependency()).thenReturn(Optional.empty());
        pluginRegistry.addPlugin(pluginMetadata);
        assertTrue(pluginRegistry.getPlugin(PLUGIN_NAME).isPresent());
        assertEquals(PLUGIN_NAME, pluginRegistry.getPlugin(PLUGIN_NAME).get().getName());
        assertFalse(pluginRegistry.getPlugin(PLUGIN_NAME).get().getProvidedDependency().isPresent());
        assertEquals(1, pluginRegistry.getPluginNames().size());
        assertEquals(PLUGIN_NAME, pluginRegistry.getPluginNames().get(0));
    }

    @Test(expected = PluginException.class)
    public void testDuplicateProvidedDependency() throws PluginException {
        PluginRegistry pluginRegistry = new PluginRegistryContainer();
        PluginMetadata pluginMetadata = mock(PluginMetadata.class);
        when(pluginMetadata.getName()).thenReturn(PLUGIN_NAME);

        Dependency pluginDependency = new DependencyContainer(PLUGIN_NAME, "plugin-version");
        when(pluginMetadata.getDependency()).thenReturn(pluginDependency);

        Dependency providedDependency = new DependencyContainer("some-plugin", "some-version");
        when(pluginMetadata.getProvidedDependency()).thenReturn(Optional.of(providedDependency));
        pluginRegistry.addPlugin(pluginMetadata);
        pluginRegistry.addPlugin(pluginMetadata); //Adding the same plugin for the second time
    }

    @Test
    public void testExtensionPointsRegistry() {
        PluginRegistry pluginRegistry = new PluginRegistryContainer();
        pluginRegistry.addImplementations(TestExtensionPoint.class, new ArrayList<Class>() {
            {
                add(TestExtensionPointImpl.class);
            }
        });
        assertEquals(1, pluginRegistry.getExtensionPoints().size());
        assertEquals(1, pluginRegistry.getImplementations(TestExtensionPoint.class).size());
        assertEquals(TestExtensionPointImpl.class, pluginRegistry.getImplementations(TestExtensionPoint.class).get(0));
    }

}
