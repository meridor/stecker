package org.meridor.stecker.interfaces;

import java.nio.file.Path;

public interface ResourceChangedHandler {

    void onResourceChanged(Path resource);

}
