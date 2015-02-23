package org.meridor.stecker.impl;

import org.meridor.stecker.interfaces.PluginImplementationsAware;
import org.meridor.stecker.interfaces.ScanResult;

public class DefaultScanResult implements ScanResult {

    private final ClassLoader classLoader;

    private final PluginImplementationsAware contents;

    public DefaultScanResult(ClassLoader classLoader, PluginImplementationsAware contents) {
        this.classLoader = classLoader;
        this.contents = contents;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public PluginImplementationsAware getContents() {
        return contents;
    }
}
