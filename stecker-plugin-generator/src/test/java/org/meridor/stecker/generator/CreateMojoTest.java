package org.meridor.stecker.generator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.meridor.stecker.impl.FileSystemHelper;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.marvinformatics.kiss.matchers.path.PathMatchers.exists;
import static com.marvinformatics.kiss.matchers.path.PathMatchers.isDirectory;
import static com.marvinformatics.kiss.matchers.path.PathMatchers.isRegularFile;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CreateMojoTest {
    
    private static final String ARTIFACT_FILE = "javax.servlet-api-3.1.0.jar";
    
    @Rule
    public MojoRule rule = new MojoRule();

    private Path targetDir;
    
    private MavenProject mavenProject;
    
    @Before
    public void prepareData() throws Exception {
        Path pomPath = getResource("pom.xml").get();
        Path baseDir = pomPath.getParent();
        targetDir = baseDir.resolve("target");
        Path classesDir = targetDir.resolve("classes");
        Path packageDir = classesDir.resolve("test-package");
        Files.createDirectories(packageDir);
        Path testClassFile = packageDir.resolve("Test.class");
        Files.createFile(testClassFile);

        mavenProject = rule.readMavenProject(baseDir.toFile());

        Path artifactFile = targetDir.resolve(ARTIFACT_FILE);
        Files.createFile(artifactFile);
        Artifact artifact = new DefaultArtifact(
                "javax.servlet",
                "javax.servlet-api",
                "3.1.0",
                "compile",
                "jar",
                null,
                new DefaultArtifactHandler()
        );
        artifact.setFile(artifactFile.toFile());
        mavenProject.getArtifacts().add(artifact);
    }
    
    @Test
    public void testExecute() throws Exception {
        CreateMojo mojo = (CreateMojo) rule.lookupConfiguredMojo(mavenProject, "create");
        assertThat(mojo, notNullValue());
        mojo.execute();
        
        Path generatedDataDir = targetDir.resolve("plugin-generator");
        assertThat(generatedDataDir, exists());
        assertThat(generatedDataDir, isDirectory());
        
        Path pluginFile = generatedDataDir.resolve("plugin-test-1.0.jar");
        assertThat(pluginFile, exists());
        assertThat(pluginFile, isRegularFile());
        
        Path pluginDataDir = generatedDataDir.resolve("data");
        assertThat(pluginDataDir, exists());
        assertThat(pluginDataDir, isDirectory());

        Path libDir = pluginDataDir.resolve("lib");
        assertThat(libDir, exists());
        assertThat(libDir, isDirectory());
        
        Path dependencyFile = libDir.resolve(ARTIFACT_FILE);
        assertThat(dependencyFile, exists());
        assertThat(dependencyFile, isRegularFile());
        
    }
    
    private Optional<Path> getResource(String resourceName) throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        return (resource != null) ? Optional.of(Paths.get(resource.toURI())) : Optional.empty();
    }
    
    @After
    public void removeData() throws Exception {
        if (targetDir != null && Files.exists(targetDir)) {
            FileSystemHelper.removeDirectory(targetDir);
        }
    }
}
