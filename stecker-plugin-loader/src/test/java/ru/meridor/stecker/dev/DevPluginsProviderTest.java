package ru.meridor.stecker.dev;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import ru.meridor.stecker.TemporaryDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DevPluginsProviderTest {

    @Rule
    public TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

    private Path baseDirectory;
    private Path matchingDirectory;

    @Before
    public void createTestFiles() throws IOException {
        baseDirectory = temporaryDirectory.getDirectory();
        matchingDirectory = baseDirectory.resolve("directory-name-containing-plugin");
        Files.createDirectories(matchingDirectory);

        Path notMatchingDirectory = baseDirectory.resolve("not-matching-directory");
        Files.createDirectories(notMatchingDirectory);

        Path matchingFile = baseDirectory.resolve("file-name-containing-plugin");
        Files.createFile(matchingFile);
    }

    @Test
    public void testProvide() throws Exception {
        DevPluginsProvider devPluginsProvider = new DevPluginsProvider();
        List<Path> pluginPaths = devPluginsProvider.provide(baseDirectory);
        assertThat(pluginPaths, hasSize(1));
        assertThat(pluginPaths, hasItem(matchingDirectory));
    }
}