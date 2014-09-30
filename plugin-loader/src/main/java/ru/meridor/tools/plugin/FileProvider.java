package ru.meridor.tools.plugin;

import java.io.File;
import java.util.List;

/**
 * Provides a set of jar files to be processed by plugin loader
 */
public interface FileProvider {

    List<File> provide() throws PluginException;

}
