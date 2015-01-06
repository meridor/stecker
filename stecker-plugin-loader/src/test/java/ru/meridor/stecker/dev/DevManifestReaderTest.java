package ru.meridor.stecker.dev;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import ru.meridor.stecker.PluginMetadata;
import ru.meridor.stecker.TemporaryDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DevManifestReaderTest {

    @Rule
    public TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

    private static final String DIRECTORY_NAME = "some-plugin";

    private Path pluginDirectory;

    @Before
    public void createDirectory() throws IOException {
        pluginDirectory = temporaryDirectory.getDirectory().resolve(DIRECTORY_NAME);
        Files.createDirectories(pluginDirectory);
    }

    @Test
    public void testRead() throws Exception {
        DevManifestReader devManifestReader = new DevManifestReader();
        PluginMetadata pluginMetadata = devManifestReader.read(pluginDirectory);
        assertThat(pluginMetadata.getName(), equalTo(DIRECTORY_NAME));
        assertThat(pluginMetadata.getVersion(), equalTo("dev"));
        assertThat(pluginMetadata.getPath(), equalTo(pluginDirectory));
    }
}