package ru.meridor.tools.plugin.impl;

import java.io.File;
import java.io.FileFilter;

public class DefaultFileFilter implements FileFilter {

    public static final String PLUGIN_SUFFIX = "-plugin.jar";

    @Override
    public boolean accept(File fileName) {
        return (fileName != null) && fileName.getAbsolutePath().endsWith(PLUGIN_SUFFIX);
    }

}
