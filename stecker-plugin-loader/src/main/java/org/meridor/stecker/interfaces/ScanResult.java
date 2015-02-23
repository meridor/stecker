package org.meridor.stecker.interfaces;

public interface ScanResult {

    ClassLoader getClassLoader();

    PluginImplementationsAware getContents();

}
