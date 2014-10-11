package ru.meridor.tools.plugin.impl.data;

public class TestExtensionPointImpl implements TestExtensionPoint {

    @Override
    public void doSomething() {
        new LibraryClass().act();
    }

}
