package ru.meridor.tools.plugin.generator;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;
import ru.meridor.tools.plugin.impl.DefaultClassesScanner;
import ru.meridor.tools.plugin.impl.DefaultManifestReader;
import ru.meridor.tools.plugin.impl.ManifestField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(
        name="create",
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresProject = true,
        threadSafe = true
)
public class CreateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.artifactId}", readonly = true, required = true)
    private String pluginName;

    @Parameter(defaultValue = "${project.version}", required = true)
    private String pluginVersion;

    @Parameter
    private String description;

    @Parameter
    private String maintainer;

    @Parameter
    private List<Plugin> depends;

    @Parameter
    private List<Plugin> conflicts;

    @Parameter
    private String provides;

    /**
     * Plugin contents are saved to this directory
     */
    @Parameter(defaultValue = "${project.build.directory}/plugin-generator/data", required = true)
    private File dataOutputDirectory;

    /**
     * Plugin contents from {@link #dataOutputDirectory} are packed to jar which is saved to this directory
     */
    @Parameter(defaultValue = "${project.build.directory}/plugin-generator", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
    private File compiledSourcesDir;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver sourcesArchiver;

    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver pluginArchiver;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info(String.format("Preparing contents for plugin: %s", pluginName));
            preparePluginContents();
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Failed to prepare contents for plugin [%s]", pluginName), e);
        }
    }

    private void preparePluginContents() throws Exception {
        copyDependencies();
        packageCompiledSources();
        packagePlugin();
    }

    private void copyDependencies() throws IOException {
        getLog().debug("Copying plugin dependencies");
        Set<Artifact> artifacts = project.getArtifacts();
        Path outputPath = Paths.get(dataOutputDirectory.toURI());
        Path libDirectory = outputPath.resolve(DefaultClassesScanner.LIB_DIRECTORY);
        getLog().debug(String.format("Creating directory to store dependencies: %s", libDirectory.toString()));
        Files.createDirectories(libDirectory);
        for (Artifact artifact: artifacts) {
            Path artifactSourcePath = Paths.get(artifact.getFile().toURI());
            Path artifactDestinationPath = libDirectory.resolve(artifactSourcePath.getFileName());
            getLog().debug(String.format("Copying %s to %s", artifactSourcePath.toString(), libDirectory.toString()));
            Files.copy(artifactSourcePath, artifactDestinationPath);
        }
    }

    private void packageCompiledSources() throws DependencyResolutionRequiredException, IOException, ManifestException {
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(sourcesArchiver);
        Path pluginJarFile = getDataOutputDirectory().resolve(DefaultClassesScanner.PLUGIN_CLASSES_FILE);
        getLog().debug(String.format("Packing plugin sources to %s", pluginJarFile.toString()));
        archiver.setOutputFile(pluginJarFile.toFile());
        sourcesArchiver.addDirectory(compiledSourcesDir);
        archiver.createArchive(session, project, new MavenArchiveConfiguration());
    }

    private void packagePlugin() throws DependencyResolutionRequiredException, IOException, ManifestException {
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(pluginArchiver);
        Path pluginFile = getPluginFile();
        getLog().info(String.format("Creating plugin file: %s", pluginFile));
        archiver.setOutputFile(pluginFile.toFile());
        pluginArchiver.addDirectory(dataOutputDirectory);
        pluginArchiver.addConfiguredManifest(getPluginManifest());
        archiver.createArchive(session, project, new MavenArchiveConfiguration());
    }

    private Manifest getPluginManifest() throws ManifestException {
        getLog().debug("Generating plugin manifest");
        Manifest manifest = new Manifest();
        addAttribute(manifest, ManifestField.NAME.getFieldName(), pluginName);
        addAttribute(manifest, ManifestField.VERSION.getFieldName(), pluginVersion);
        addAttribute(manifest, ManifestField.DATE.getFieldName(), ZonedDateTime.now().format(DefaultManifestReader.DATE_FORMATTER));
        if (description != null){
            addAttribute(manifest, ManifestField.DESCRIPTION.getFieldName(), description);
        }
        if (maintainer != null){
            addAttribute(manifest, ManifestField.MAINTAINER.getFieldName(), maintainer);
        }
        if (depends != null){
            addAttribute(manifest, ManifestField.DEPENDS.getFieldName(), joinDependencies(depends));
        }
        if (conflicts != null){
            addAttribute(manifest, ManifestField.CONFLICTS.getFieldName(), joinDependencies(conflicts));
        }
        if (provides != null){
            addAttribute(manifest, ManifestField.PROVIDES.getFieldName(), provides);
        }
        return manifest;
    }

    private static String joinDependencies(List<Plugin> dependencies) {
        return dependencies
                .stream()
                .map(pl ->
                        (pl.getVersion() != null) ?
                        pl.getName() + DefaultManifestReader.VERSION_DELIMITER + pl.getVersion() :
                        pl.getName()
                )
                .collect(Collectors.joining(DefaultManifestReader.DEPENDENCY_DELIMITER));
    }

    private void addAttribute(Manifest manifest, String name, String value) throws ManifestException {
        getLog().debug(String.format("Setting manifest field %s to %s", name, value));
        manifest.getMainSection().addConfiguredAttribute(new Manifest.Attribute(name, value));
    }

    private Path getDataOutputDirectory() {
        return Paths.get(dataOutputDirectory.toURI());
    }

    private Path getPluginFile() {
        return Paths.get(outputDirectory.toURI()).resolve(pluginName + "-" + pluginVersion + ".jar");
    }

}
