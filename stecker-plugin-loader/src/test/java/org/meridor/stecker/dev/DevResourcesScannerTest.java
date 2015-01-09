package org.meridor.stecker.dev;

import org.junit.Rule;
import org.junit.Test;
import org.meridor.stecker.PluginException;
import org.meridor.stecker.TemporaryDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DevResourcesScannerTest {

    @Rule
    public TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

    private static final String[] GLOBS = new String[]{"glob:**/*.txt"};

    private Path matchingFile;

    @Test
    public void testScan() throws Exception {
        createFiles();
        Path pluginDirectory = temporaryDirectory.getDirectory();
        DevResourcesScanner devResourcesScanner = new DevResourcesScanner(GLOBS);
        List<Path> matchingResources = devResourcesScanner.scan(pluginDirectory);
        assertThat(matchingResources, hasSize(1));
        assertThat(matchingResources, hasItem(matchingFile));
    }

    @Test
    public void testResourcesDirectoryMissing() throws PluginException {
        Path pluginDirectory = temporaryDirectory.getDirectory();
        DevResourcesScanner devResourcesScanner = new DevResourcesScanner(GLOBS);
        List<Path> matchingResources = devResourcesScanner.scan(pluginDirectory);
        assertThat(matchingResources, hasSize(0));

    }

    private void createFiles() throws IOException {
        Path resourcesDir = temporaryDirectory.getDirectory().resolve("src").resolve("main").resolve("resources");
        Files.createDirectories(resourcesDir);

        matchingFile = resourcesDir.resolve("textfile.txt");
        Files.createFile(matchingFile);

        Path notMatchingFile = resourcesDir.resolve("picture.jpg");
        Files.createFile(notMatchingFile);
    }
}