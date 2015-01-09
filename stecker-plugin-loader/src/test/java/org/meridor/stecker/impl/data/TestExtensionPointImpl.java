package org.meridor.stecker.impl.data;

public class TestExtensionPointImpl implements TestExtensionPoint {

    @Override
    public void doSomething() {
        new LibraryClass().act();
    }

}
