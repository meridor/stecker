package org.meridor.stecker.impl;

import org.junit.Test;
import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;
import org.meridor.stecker.PluginRegistry;
import org.meridor.stecker.impl.data.TestExtensionPoint;
import org.meridor.stecker.impl.data.TestExtensionPointImpl;
import org.meridor.stecker.interfaces.Dependency;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginRegistryContainerTest {

    private static final String PLUGIN_NAME = "test-plugin";
    private static final String ANOTHER_PLUGIN_NAME = "another-test-plugin";
    private static final String MISSING_PLUGIN_NAME = "missing-plugin";

    @Test
    public void testPluginsRegistry() throws PluginException {
        PluginRegistry pluginRegistry = new PluginRegistryContainer();
        PluginMetadata pluginMetadata = getMockPluginMetadata(PLUGIN_NAME);
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
        PluginMetadata pluginMetadata = getMockPluginMetadata(PLUGIN_NAME);

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
        
        //One plugin
        PluginMetadata pluginMetadata = getMockPluginMetadata(PLUGIN_NAME);
        pluginRegistry.addImplementations(pluginMetadata, TestExtensionPoint.class, new ArrayList<Class>() {
            {
                add(TestExtensionPointImpl.class);
            }
        });
        
        //Another plugin
        PluginMetadata anotherPluginMetadata = getMockPluginMetadata(ANOTHER_PLUGIN_NAME);
        pluginRegistry.addImplementations(anotherPluginMetadata, TestExtensionPoint.class, new ArrayList<Class>() {
            {
                add(String.class);
            }
        });
        pluginRegistry.addImplementations(anotherPluginMetadata, Number.class, new ArrayList<Class>() {
            {
                add(Integer.class);
                add(AtomicInteger.class);
            }
        });
        
        assertThat(pluginRegistry.getExtensionPoints(), hasSize(2));
        assertThat(pluginRegistry.getImplementations(TestExtensionPoint.class), hasSize(2));
        assertThat(pluginRegistry.getImplementations(TestExtensionPoint.class), containsInAnyOrder(TestExtensionPointImpl.class, String.class));
        assertThat(pluginRegistry.getImplementations(Number.class), hasSize(2));
        assertThat(pluginRegistry.getImplementations(Number.class), containsInAnyOrder(Integer.class, AtomicInteger.class));

        assertThat(pluginRegistry.getExtensionPoints(MISSING_PLUGIN_NAME), empty());
        assertThat(pluginRegistry.getImplementations(MISSING_PLUGIN_NAME, TestExtensionPoint.class), empty());
        
        assertThat(pluginRegistry.getExtensionPoints(PLUGIN_NAME), hasSize(1));
        assertThat(pluginRegistry.getExtensionPoints(PLUGIN_NAME), contains(TestExtensionPoint.class));
        assertThat(pluginRegistry.getImplementations(PLUGIN_NAME, TestExtensionPoint.class), hasSize(1));
        assertThat(pluginRegistry.getImplementations(PLUGIN_NAME, TestExtensionPoint.class), contains(TestExtensionPointImpl.class));

        assertThat(pluginRegistry.getExtensionPoints(ANOTHER_PLUGIN_NAME), hasSize(2));
        assertThat(pluginRegistry.getExtensionPoints(ANOTHER_PLUGIN_NAME), containsInAnyOrder(TestExtensionPoint.class, Number.class));
        assertThat(pluginRegistry.getImplementations(ANOTHER_PLUGIN_NAME, TestExtensionPoint.class), hasSize(1));
        assertThat(pluginRegistry.getImplementations(ANOTHER_PLUGIN_NAME, TestExtensionPoint.class), contains(String.class));
        assertThat(pluginRegistry.getImplementations(ANOTHER_PLUGIN_NAME, Number.class), hasSize(2));
        assertThat(pluginRegistry.getImplementations(ANOTHER_PLUGIN_NAME, Number.class), containsInAnyOrder(Integer.class, AtomicInteger.class));
    }
    
    private PluginMetadata getMockPluginMetadata(String pluginName) {
        PluginMetadata pluginMetadata = mock(PluginMetadata.class);
        when(pluginMetadata.getName()).thenReturn(pluginName);
        return pluginMetadata;
    }

}
