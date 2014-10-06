package ru.meridor.tools.plugin.impl;

import java.io.File;
import java.io.FileFilter;

public class DefaultFileFilter implements FileFilter {

    @Override
    public boolean accept(File fileName) {
        return (fileName != null) &&
                fileName.getName().contains("plugin") &&
                fileName.getName().endsWith("jar");
    }

}
