package ru.meridor.stecker.interfaces;


import ru.meridor.stecker.PluginException;

import java.nio.file.Path;
import java.util.List;

public interface ResourcesScanner {

    List<Path> scan(Path pluginFile) throws PluginException;

}
