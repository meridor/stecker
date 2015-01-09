package org.meridor.stecker.impl;

import org.junit.Test;
import org.meridor.stecker.JarHelper;
import org.meridor.stecker.PluginException;
import org.meridor.stecker.PluginMetadata;
import org.meridor.stecker.interfaces.Dependency;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class DefaultManifestReaderTest extends DefaultManifestReader {

    private static final String NAME = "test-name";
    private static final String VERSION = "test-version";
    private static final String DATE = "2014-09-03T09:21:30+03:00";
    private static final String DESCRIPTION = "some-description";
    private static final String MAINTAINER = "Test Maintainer <test@example.com>";
    private static final String DEPENDS = "some-plugin" + DEPENDENCY_DELIMITER +
            " another-plugin " + VERSION_DELIMITER + "[0.9,)";
    private static final String CONFLICTS = "some-conflicting-plugin" + VERSION_DELIMITER + "(,1.0]" +
            DEPENDENCY_DELIMITER + " another-conflicting-plugin " + VERSION_DELIMITER + "[1.1,1.2]";
    private static final String PROVIDES = "meta-plugin";

    private static final Map<String, String> manifestContents = new HashMap<String, String>() {
        {
            put(ManifestField.NAME.getFieldName(), NAME);
            put(ManifestField.VERSION.getFieldName(), VERSION);
            put(ManifestField.DATE.getFieldName(), DATE);
            put(ManifestField.DESCRIPTION.getFieldName(), DESCRIPTION);
            put(ManifestField.MAINTAINER.getFieldName(), MAINTAINER);
            put(ManifestField.DEPENDS.getFieldName(), DEPENDS);
            put(ManifestField.CONFLICTS.getFieldName(), CONFLICTS);
            put(ManifestField.PROVIDES.getFieldName(), PROVIDES);
        }
    };

    @Override
    protected Manifest getManifest(Path pluginFile) throws IOException {
        return JarHelper.createManifest(manifestContents);
    }

    @Test
    public void testRead() throws PluginException {
        final Path FILE = Paths.get("missing-file");
        PluginMetadata pluginMetadata = read(FILE);
        assertThat(pluginMetadata.getName(), equalTo(NAME));
        assertThat(pluginMetadata.getVersion(), equalTo(VERSION));
        assertThat(pluginMetadata.getDate(), equalTo(Optional.of(ZonedDateTime.parse(DATE))));
        assertThat(pluginMetadata.getDescription(), equalTo(Optional.of(DESCRIPTION)));
        assertThat(pluginMetadata.getMaintainer(), equalTo(Optional.of(MAINTAINER)));
        assertThat(pluginMetadata.getDependency(), equalTo(new DependencyContainer(NAME, VERSION)));
        assertThat(pluginMetadata.getPath(), equalTo(FILE));

        List<Dependency> requiredDependencies = pluginMetadata.getRequiredDependencies();
        assertThat(requiredDependencies, hasSize(2));
        Dependency requiredDependency1 = new DependencyContainer("some-plugin");
        Dependency requiredDependency2 = new DependencyContainer("another-plugin", "[0.9,)");
        assertThat(requiredDependencies, contains(requiredDependency1, requiredDependency2));

        List<Dependency> conflictingDependencies = pluginMetadata.getConflictingDependencies();
        assertThat(conflictingDependencies, hasSize(2));
        Dependency conflictingDependency1 = new DependencyContainer("some-conflicting-plugin", "(,1.0]");
        Dependency conflictingDependency2 = new DependencyContainer("another-conflicting-plugin", "[1.1,1.2]");
        assertThat(conflictingDependencies, contains(conflictingDependency1, conflictingDependency2));

        Dependency provided = new DependencyContainer(PROVIDES);
        assertThat(pluginMetadata.getProvidedDependency(), equalTo(Optional.of(provided)));
    }

    @Test
    public void testGetField() {
        Manifest manifest = JarHelper.createManifest(new HashMap<String, String>() {
            {
                put(ManifestField.NAME.getFieldName(), NAME);
            }
        });
        assertThat(getField(manifest, ManifestField.NAME), equalTo(NAME));
        assertNull(getField(manifest, ManifestField.VERSION)); //Missing field
    }

    @Test(expected = ManifestException.class)
    public void testGetMissingRequireField() throws PluginException, ManifestException {
        getRequiredField(new Manifest(), ManifestField.NAME);
    }

    @Test
    public void testGetDependenciesList() throws ManifestException {
        Manifest manifest = JarHelper.createManifest(manifestContents);
        List<Dependency> list = getDependenciesList(manifest, ManifestField.DEPENDS);
        assertThat(list, hasSize(2));
        assertThat(list, contains(
                new DependencyContainer("some-plugin"),
                new DependencyContainer("another-plugin", "[0.9,)")
        ));
    }

    @Test
    public void testGetEmptyDependenciesList() throws ManifestException {
        assertThat(getDependenciesList(new Manifest(), ManifestField.DEPENDS), empty());
    }

    @Test(expected = ManifestException.class)
    public void testGetIncorrectDependency() throws ManifestException {
        getDependency("some" + VERSION_DELIMITER + "incorrect" + VERSION_DELIMITER + "dependency");
    }

    @Test(expected = ManifestException.class)
    public void testGetDateField() throws PluginException, ManifestException {
        Manifest manifest = JarHelper.createManifest(new HashMap<String, String>() {
            {
                put(ManifestField.DATE.getFieldName(), "invalid-date");
            }
        });
        assertFalse(getDateField(manifest, ManifestField.NAME).isPresent()); //Missing field
        getDateField(manifest, ManifestField.DATE); //Throws exception
    }
}
