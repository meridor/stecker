package ru.meridor.stecker.impl;

import org.junit.Test;
import ru.meridor.stecker.PluginException;
import ru.meridor.stecker.PluginMetadata;
import ru.meridor.stecker.PluginRegistry;
import ru.meridor.stecker.impl.data.TestExtensionPoint;
import ru.meridor.stecker.impl.data.TestExtensionPointImpl;
import ru.meridor.stecker.interfaces.Dependency;

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
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
        assertThat(pluginRegistry.getPlugin(PLUGIN_NAME).get().getName(), equalTo(PLUGIN_NAME));
        assertFalse(pluginRegistry.getPlugin(PLUGIN_NAME).get().getProvidedDependency().isPresent());
        assertThat(pluginRegistry.getPluginNames(), hasSize(1));
        assertThat(pluginRegistry.getPluginNames(), contains(PLUGIN_NAME));
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
        assertThat(pluginRegistry.getExtensionPoints(), hasSize(1));
        assertThat(pluginRegistry.getImplementations(TestExtensionPoint.class), hasSize(1));
        assertThat(pluginRegistry.getImplementations(TestExtensionPoint.class), contains(TestExtensionPointImpl.class));
    }

}
