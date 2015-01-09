package org.meridor.stecker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.meridor.stecker.interfaces.ResourceChangedHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ResourcesWatcherTest {

    @Rule
    public TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

    private ResourcesWatcher resourcesWatcher;

    private Path temporaryFile;

    private TestHandler testHandler;

    @Before
    public void before() throws IOException {
        final String FILE_NAME = "test.file";
        temporaryFile = temporaryDirectory.createFile(FILE_NAME, randomString().getBytes());
        resourcesWatcher = new ResourcesWatcher(Arrays.asList(temporaryFile));
        testHandler = new TestHandler();
        resourcesWatcher.addChangedHandler(testHandler);
    }

    @Test(timeout = 10000)
    public void testAwaitAnyChange() throws Exception {
        scheduleFileChange();
        resourcesWatcher.await();
        assertThat(testHandler.getChangesCount(), equalTo(1));
    }

    @Test(timeout = 10000)
    public void testAwaitConcreteChange() throws Exception {
        scheduleFileChange();
        resourcesWatcher.await(temporaryFile);
        assertThat(testHandler.getChangesCount(), equalTo(1));
        Thread.sleep(1500);
        assertThat(testHandler.getChangesCount(), equalTo(1)); //Event should occur only once

    }

    private void scheduleFileChange() {
        final long SCHEDULE_DELAY = 1000;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Files.write(temporaryFile, randomString().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, SCHEDULE_DELAY);
    }

    private String randomString() {
        return String.valueOf(1e6 * Math.random());
    }

    @After
    public void after() {
        if (resourcesWatcher != null) {
            resourcesWatcher.stop();
        }
    }

    private static class TestHandler implements ResourceChangedHandler {

        private int changesCount = 0;

        @Override
        public void onResourceChanged(Path resource) {
            changesCount++;
        }

        public int getChangesCount() {
            return changesCount;
        }
    }

}