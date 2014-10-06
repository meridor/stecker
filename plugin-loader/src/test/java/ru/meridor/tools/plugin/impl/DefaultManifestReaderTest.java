package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import ru.meridor.tools.plugin.Dependency;
import ru.meridor.tools.plugin.JarHelper;
import ru.meridor.tools.plugin.PluginException;
import ru.meridor.tools.plugin.PluginMetadata;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;

import static org.junit.Assert.*;

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

    private static final Map<String, String> manifestContents = new HashMap<String, String>(){
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
    protected Manifest getManifest(File pluginFile) throws IOException {
        return JarHelper.createManifest(manifestContents);
    }

    @Test
    public void testRead() throws PluginException {
        final File FILE = new File("missing-file");
        PluginMetadata pluginMetadata = read(FILE);
        assertEquals(NAME, pluginMetadata.getName());
        assertEquals(VERSION, pluginMetadata.getVersion());
        assertEquals(Optional.of(ZonedDateTime.parse(DATE)), pluginMetadata.getDate());
        assertEquals(Optional.of(DESCRIPTION), pluginMetadata.getDescription());
        assertEquals(Optional.of(MAINTAINER), pluginMetadata.getMaintainer());
        assertEquals(new DependencyContainer(NAME, VERSION), pluginMetadata.getDependency());
        assertEquals(FILE, pluginMetadata.getFile());

        List<Dependency> requiredDependencies = pluginMetadata.getRequiredDependencies();
        assertEquals(2, requiredDependencies.size());
        Dependency requiredDependency1 = new DependencyContainer("some-plugin");
        assertEquals(requiredDependency1, requiredDependencies.get(0));
        Dependency requiredDependency2 = new DependencyContainer("another-plugin", "[0.9,)");
        assertEquals(requiredDependency2, requiredDependencies.get(1));

        List<Dependency> conflictingDependencies = pluginMetadata.getConflictingDependencies();
        assertEquals(2, conflictingDependencies.size());
        Dependency conflictingDependency1 = new DependencyContainer("some-conflicting-plugin", "(,1.0]");
        assertEquals(conflictingDependency1, conflictingDependencies.get(0));
        Dependency conflictingDependency2 = new DependencyContainer("another-conflicting-plugin", "[1.1,1.2]");
        assertEquals(conflictingDependency2, conflictingDependencies.get(1));

        Dependency provided = new DependencyContainer(PROVIDES);
        assertEquals(Optional.of(provided), pluginMetadata.getProvidedDependency());
    }

    @Test
    public void testGetField() {
        Manifest manifest = JarHelper.createManifest(new HashMap<String, String>(){
            {
                put(ManifestField.NAME.getFieldName(), NAME);
            }
        });
        assertEquals(Optional.of(NAME), getField(manifest, ManifestField.NAME));
        assertFalse(getField(manifest, ManifestField.VERSION).isPresent()); //Missing field
    }

    @Test(expected = ManifestException.class)
    public void testGetMissingRequireField() throws PluginException, ManifestException {
        getRequiredField(new Manifest(), ManifestField.NAME);
    }

    @Test
    public void testGetDependenciesList() throws ManifestException {
        Manifest manifest = JarHelper.createManifest(manifestContents);
        List<Dependency> list = getDependenciesList(manifest, ManifestField.DEPENDS);
        assertEquals(2, list.size());
        assertEquals(new DependencyContainer("some-plugin"), list.get(0));
        assertEquals(new DependencyContainer("another-plugin", "[0.9,)"), list.get(1));
    }

    @Test
    public void testGetEmptyDependenciesList() throws ManifestException {
        assertTrue(getDependenciesList(new Manifest(), ManifestField.DEPENDS).isEmpty());
    }

    @Test(expected = ManifestException.class)
    public void testGetIncorrectDependency() throws ManifestException {
        getDependency("some" + VERSION_DELIMITER + "incorrect" + VERSION_DELIMITER + "dependency");
    }

    @Test(expected = ManifestException.class)
    public void testGetDateField() throws PluginException, ManifestException {
        Manifest manifest = JarHelper.createManifest(new HashMap<String, String>(){
            {
                put(ManifestField.DATE.getFieldName(), "invalid-date");
            }
        });
        assertFalse(getDateField(manifest, ManifestField.NAME).isPresent()); //Missing field
        getDateField(manifest, ManifestField.DATE); //Throws exception
    }
}
