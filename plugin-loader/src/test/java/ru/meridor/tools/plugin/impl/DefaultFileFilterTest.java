package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DefaultFileFilterTest {

    private final String fileName;

    private final boolean accept;

    @Parameterized.Parameters(name = "name = {0} must give accept = {1}")
    public static Collection combinations() {
        return Arrays.asList(new Object[][]{
                {"something" + DefaultFileFilter.PLUGIN_SUFFIX, true},
                {"something" + DefaultFileFilter.PLUGIN_SUFFIX + "End", false},
                {"not-matching-name", false}
        });
    }

    public DefaultFileFilterTest(String fileName, boolean accept) {
        this.accept = accept;
        this.fileName = fileName;
    }

    @Test
    public void testAccept() {
        assertEquals(accept, new DefaultFileFilter().accept(new File(fileName)));
    }

}
