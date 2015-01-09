package org.meridor.stecker;

import org.meridor.stecker.impl.PluginUtils;
import org.meridor.stecker.interfaces.ResourceChangedHandler;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Allows you to watch for resource files changes
 */
public class ResourcesWatcher implements Closeable {

    private static final String THREAD_NAME = "Stecker Resources Watcher";

    private volatile List<ResourceChangedHandler> handlers = new ArrayList<>();

    private volatile boolean isStarted;

    private Thread watcherThread;

    public ResourcesWatcher(List<Path> resources) {
        this.watcherThread = getWatcherThread(resources);
    }

    private Thread getWatcherThread(List<Path> resources) {
        return isMacOs() ? getScanningWatcherThread(resources) : getWatchServiceWatcherThread(resources);
    }

    private static boolean isMacOs() {
        //WatchService doesn't work correctly on MacOS
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    private Thread getWatchServiceWatcherThread(List<Path> resources) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                    final List<Path> alreadyRegisteredDirectories = new ArrayList<>();
                    List<Path> regularFilesAndDirectories = resources.stream()
                            .filter(r -> Files.isDirectory(r) || Files.isRegularFile(r))
                            .collect(Collectors.toList());
                    for (Path resource : regularFilesAndDirectories) {
                        Path pathToRegister = resource;
                        if (Files.isRegularFile(resource)) {
                            pathToRegister = resource.getParent();
                        }
                        if (!alreadyRegisteredDirectories.contains(pathToRegister)) {
                            pathToRegister.register(watcher, ENTRY_MODIFY);
                            alreadyRegisteredDirectories.add(pathToRegister);
                        }
                    }
                    while (true) {
                        WatchKey key;
                        try {
                            key = watcher.take();
                        } catch (InterruptedException e) {
                            break;
                        }
                        Path directory = (Path) key.watchable();
                        key.pollEvents()
                                .stream()
                                .filter(event -> ENTRY_MODIFY == event.kind())
                                .forEach(event -> {
                                    for (ResourceChangedHandler handler : handlers) {
                                        Path filePath = directory.resolve((Path) event.context());
                                        handler.onResourceChanged(filePath);
                                    }
                                });
                        if (!key.reset()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        thread.setName(THREAD_NAME);
        thread.setDaemon(true);
        return thread;
    }

    private Thread getScanningWatcherThread(List<Path> resources) {
        final long SCAN_DELAY = 1000;
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Map<Path, FileTime> lastModifiedTimes = new HashMap<>();
                    for (Path resource : resources) {
                        lastModifiedTimes.put(resource, PluginUtils.getLastModificationTime(resource));
                    }

                    while (true) {
                        try {
                            for (Path resource : resources) {
                                FileTime previousLastModifiedTime = lastModifiedTimes.get(resource);
                                FileTime currentLastModifiedTime = PluginUtils.getLastModificationTime(resource);
                                if (currentLastModifiedTime.compareTo(previousLastModifiedTime) > 0) {
                                    lastModifiedTimes.put(resource, currentLastModifiedTime);
                                    for (ResourceChangedHandler handler : handlers) {
                                        handler.onResourceChanged(resource);
                                    }
                                }
                            }
                            sleep(SCAN_DELAY);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        thread.setName(THREAD_NAME);
        thread.setDaemon(true);
        return thread;
    }

    public void addChangedHandler(ResourceChangedHandler handler) {
        handlers.add(handler);
    }

    public void start() {
        watcherThread.start();
        isStarted = true;
    }

    public void stop() {
        if (isStarted) {
            watcherThread.interrupt();
            isStarted = false;
        }
    }

    public void await(Path resource) throws InterruptedException {
        if (!isStarted) {
            start();
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        addChangedHandler(r -> {
            if (resource == null || r.equals(resource)) {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        stop();
    }

    public void await() throws InterruptedException {
        await(null);
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}
