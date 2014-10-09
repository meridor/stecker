package ru.meridor.tools.plugin.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.meridor.tools.plugin.JarHelper;
import ru.meridor.tools.plugin.Plugin;
import ru.meridor.tools.plugin.impl.data.PluginImpl;
import ru.meridor.tools.plugin.impl.data.TestExtensionPoint;
import ru.meridor.tools.plugin.impl.data.TestExtensionPointImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultClassesScannerTest {
    
    private Path tempDirectory;

    @Before
    public void createTempDirectory() throws IOException {
        tempDirectory = FileSystemHelper.createTempDirectory();
    }

    @Test
    public void testScan() throws Exception {
        Path cacheDirectory = tempDirectory.resolve(".cache");
        Path pluginFile = JarHelper.createTestPluginFile(tempDirectory, Optional.empty());
        List<Class> extensionPoints = new ArrayList<Class>(){
            {
                add(TestExtensionPoint.class);
            }
        };
        
        Map<Class, List<Class>> classesMap = new DefaultClassesScanner(cacheDirectory).scan(pluginFile, extensionPoints);
        
        assertEquals(2, classesMap.entrySet().size());
        assertTrue(classesMap.containsKey(Plugin.class));
        
        List<Class> pluginImplementations = classesMap.get(Plugin.class);
        assertEquals(1, pluginImplementations.size());
        assertEquals(PluginImpl.class, pluginImplementations.get(0));
        assertTrue(classesMap.containsKey(TestExtensionPoint.class));
        
        List<Class> testExtensionPointImplementations = classesMap.get(TestExtensionPoint.class);
        assertEquals(1, testExtensionPointImplementations.size());
        assertEquals(TestExtensionPointImpl.class, testExtensionPointImplementations.get(0));
    }
    
    @After
    public void removeTempDirectory() throws IOException {
        FileSystemHelper.removeDirectory(tempDirectory);
    }
    
}
