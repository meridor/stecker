package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import ru.meridor.tools.plugin.ClassesScanner;
import ru.meridor.tools.plugin.JarHelper;
import ru.meridor.tools.plugin.Plugin;
import ru.meridor.tools.plugin.PluginException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class DefaultClassesScannerTest {

    private static ClassesScanner getDefaultClassesScanner() throws IOException {
        DefaultClassesScanner defaultClassesScanner = new DefaultClassesScanner(
                DefaultClassesScanner.class.getClassLoader()
        );
        DefaultClassesScanner spy = spy(defaultClassesScanner);
        JarFile mockedJarFile = JarHelper.getMockedJarFile(PluginImpl.class, TestExtensionPointImpl.class);
        doReturn(mockedJarFile).when(spy).getJarFile(any());
        return spy;
    }

    @Test
    public void testScan() throws IOException, PluginException {
        Map<Class, List<Class>> classesMap = getDefaultClassesScanner().scan(new ArrayList<Class>(){
            {
                add(TestExtensionPoint.class);
            }
        }, new File("any-file"));
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
}
