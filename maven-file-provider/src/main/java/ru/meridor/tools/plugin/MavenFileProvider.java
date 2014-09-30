package ru.meridor.tools.plugin;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsBuildingException;
import ru.yandex.qatools.clay.Aether;
import ru.yandex.qatools.clay.maven.settings.MavenNotFoundException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ru.yandex.qatools.clay.Aether.MAVEN_CENTRAL_URL;
import static ru.yandex.qatools.clay.Aether.aether;
import static ru.yandex.qatools.clay.maven.settings.FluentProfileBuilder.newProfile;
import static ru.yandex.qatools.clay.maven.settings.FluentRepositoryBuilder.newRepository;
import static ru.yandex.qatools.clay.maven.settings.FluentSettingsBuilder.newSystemSettings;

public class MavenFileProvider implements FileProvider {

    private final String[] artifacts;
    
    private final File localRepoDir;
    
    public MavenFileProvider(File localRepoDir, String... artifacts) {
        this.artifacts = artifacts;
        this.localRepoDir = localRepoDir;
    }

    @Override
    public List<File> provide() throws PluginException {

        if (artifacts == null){
            throw new PluginException("No artifacts provided");
        }

        try {
            Aether aether = aether(localRepoDir, mavenSettings());

            List<File> fileList = new ArrayList<>();
            for (String artifact: artifacts) {
                for (URL url: aether.resolve(artifact, true).getAsUrls()) {
                    fileList.add(new File(url.toURI()));
                }
            }
            return fileList;
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    private Settings mavenSettings() throws SettingsBuildingException, MavenNotFoundException {
            return newSystemSettings()
                    .withActiveProfile(
                            newProfile()
                                    .withId("profile")
                                    .withRepository(newRepository().withUrl(MAVEN_CENTRAL_URL))
                    ).build();
    }
    
}
