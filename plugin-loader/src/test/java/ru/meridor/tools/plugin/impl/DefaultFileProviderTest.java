package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.meridor.tools.plugin.PluginException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DefaultFileProviderTest {

    private final File directory;

    private final List<File> files;

    @Parameterized.Parameters(
            name = "should return {1} for directory {0}"
    )
    public static Collection combinations() throws IOException {
        return Arrays.asList(new Object[][]{
                {null, Collections.emptyList()},
                {getMockedFile(false), Collections.emptyList()},
                {getMockedFile(true), Collections.emptyList()},
                {getMockedDirectory(), Arrays.asList(FILES)}
        });
    }

    private static File getMockedFile(boolean exists) {
        File file = mock(File.class);
        when(file.isDirectory()).thenReturn(false);
        when(file.exists()).thenReturn(exists);
        return file;
    }

    private static final File[] FILES =  new File[] {
            new File("file1"),
            new File("file2")
    };

    private static File getMockedDirectory() {
        File directory = mock(File.class);
        when(directory.isDirectory()).thenReturn(true);
        when(directory.exists()).thenReturn(true);
        when(directory.listFiles()).thenReturn(FILES);
        return directory;
    }

    public DefaultFileProviderTest(File directory, List<File> files) {
        this.directory = directory;
        this.files = files;
    }

    @Test
    public void testProvide() throws PluginException {
        assertEquals(files, new DefaultFileProvider(directory).provide());
    }

}
