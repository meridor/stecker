package ru.meridor.stecker;

import org.junit.rules.ExternalResource;
import ru.meridor.stecker.impl.FileSystemHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemporaryDirectory extends ExternalResource {

    private Path directory;

    @Override
    protected void before() throws Throwable {
        super.before();
        create();
    }

    private void create() throws IOException {
        directory = FileSystemHelper.createTempDirectory();
    }

    @Override
    protected void after() {
        super.after();
        remove();
    }

    private void remove() {
        try {
            FileSystemHelper.removeDirectory(directory);
        } catch (IOException e) {
            System.out.println("Can't remove temporary directory");
            e.printStackTrace();
        }
    }

    public Path getDirectory() {
        return directory;
    }

    public Path createFile(String fileName, byte[] contents) throws IOException {
        Path filePath = directory.resolve(fileName);
        Files.write(filePath, contents);
        return filePath;
    }
}
