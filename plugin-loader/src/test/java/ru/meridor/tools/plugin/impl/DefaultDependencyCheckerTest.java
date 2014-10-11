package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import ru.meridor.tools.plugin.Dependency;
import ru.meridor.tools.plugin.PluginException;
import ru.meridor.tools.plugin.PluginMetadata;
import ru.meridor.tools.plugin.PluginRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DefaultDependencyCheckerTest {

    private static final String MISSING_NAME = "missing";
    private static final Dependency MISSING_DEPENDENCY = new DependencyContainer(MISSING_NAME);
    private static final String FIXED_VERSION_NAME = "fixed";
    private static final Dependency DEPENDENCY_WITH_FIXED_VERSION = new DependencyContainer(FIXED_VERSION_NAME, "1.0");
    private static final String VERSION_RANGE_NAME = "range";
    private static final Dependency DEPENDENCY_WITH_VERSION_RANGE = new DependencyContainer(VERSION_RANGE_NAME, "[1.0,1.1]");

    private static PluginMetadata getPluginDependenciesMetadata(final Dependency[] requiredDependencies, final Dependency[] conflictingDependencies) {
        PluginMetadata pluginMetadata = mock(PluginMetadata.class);
        when(pluginMetadata.getRequiredDependencies()).thenReturn(new ArrayList<Dependency>() {
            {
                addAll(Arrays.asList(requiredDependencies));
            }
        });
        when(pluginMetadata.getConflictingDependencies()).thenReturn(new ArrayList<Dependency>() {
            {
                addAll(Arrays.asList(conflictingDependencies));
            }
        });
        return pluginMetadata;
    }

    private static Optional<PluginMetadata> getPluginIdentityMetadata(String name) {
        PluginMetadata pluginMetadata = mock(PluginMetadata.class);
        when(pluginMetadata.getName()).thenReturn(name);
        when(pluginMetadata.getVersion()).thenReturn("1.1");
        return Optional.of(pluginMetadata);
    }

    @Test
    public void testRequiredDependencyMissing() {
        PluginRegistry pluginRegistry = mock(PluginRegistry.class);
        doReturn(Optional.empty()).when(pluginRegistry).getPlugin(eq(MISSING_NAME));
        doReturn(getPluginIdentityMetadata(FIXED_VERSION_NAME))
                .when(pluginRegistry).getPlugin(eq(FIXED_VERSION_NAME));
        doReturn(getPluginIdentityMetadata(VERSION_RANGE_NAME))
                .when(pluginRegistry).getPlugin(eq(VERSION_RANGE_NAME));
        List<Dependency> missingDependencies = new ArrayList<>();
        try {
            new DefaultDependencyChecker().check(pluginRegistry, getPluginDependenciesMetadata(new Dependency[]{
                    MISSING_DEPENDENCY, DEPENDENCY_WITH_FIXED_VERSION, DEPENDENCY_WITH_VERSION_RANGE
            }, new Dependency[0]));
        } catch (PluginException e) {
            assertTrue(e.getDependencyProblem().isPresent());
            assertTrue(e.getPluginMetadata().isPresent());
            missingDependencies.addAll(e.getDependencyProblem().get().getMissingDependencies());
        }
        assertEquals(2, missingDependencies.size());
        assertTrue(missingDependencies.contains(MISSING_DEPENDENCY));
        assertTrue(missingDependencies.contains(DEPENDENCY_WITH_FIXED_VERSION));
    }

    @Test
    public void testConflictingDependencyPresent() {
        PluginRegistry pluginRegistry = mock(PluginRegistry.class);
        doReturn(getPluginIdentityMetadata(FIXED_VERSION_NAME))
                .when(pluginRegistry).getPlugin(eq(FIXED_VERSION_NAME));
        doReturn(getPluginIdentityMetadata(VERSION_RANGE_NAME))
                .when(pluginRegistry).getPlugin(eq(VERSION_RANGE_NAME));
        List<Dependency> conflictingDependencies = new ArrayList<>();
        try {
            new DefaultDependencyChecker().check(pluginRegistry, getPluginDependenciesMetadata(new Dependency[0], new Dependency[]{
                    DEPENDENCY_WITH_FIXED_VERSION, DEPENDENCY_WITH_VERSION_RANGE
            }));
        } catch (PluginException e) {
            assertTrue(e.getDependencyProblem().isPresent());
            conflictingDependencies.addAll(e.getDependencyProblem().get().getConflictingDependencies());
        }
        assertEquals(1, conflictingDependencies.size());
        assertTrue(conflictingDependencies.contains(DEPENDENCY_WITH_VERSION_RANGE));
    }

}
