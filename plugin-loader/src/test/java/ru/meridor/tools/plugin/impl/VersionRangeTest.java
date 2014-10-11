package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class VersionRangeTest {

    private final String range;
    private final String startVersion;
    private final boolean isStartVersionIncluded;
    private final String endVersion;
    private final boolean isEndVersionIncluded;
    private final String testVersion;
    private final boolean contains;
    private final boolean isValid;

    public VersionRangeTest(
            String range,
            boolean isValid,
            String startVersion,
            boolean isStartVersionIncluded,
            String endVersion,
            boolean isEndVersionIncluded,
            String testVersion,
            boolean contains
    ) {
        this.range = range;
        this.isValid = isValid;
        this.startVersion = startVersion;
        this.isStartVersionIncluded = isStartVersionIncluded;
        this.endVersion = endVersion;
        this.isEndVersionIncluded = isEndVersionIncluded;
        this.testVersion = testVersion;
        this.contains = contains;
    }

    @Parameterized.Parameters(
            name = "range = {0} should have valid = {1}, " +
                    "startVersion = {2}, isStartVersionIncluded = {3}, " +
                    "endVersion = {4}, isEndVersionIncluded = {5}, " +
                    "testVersion = {6} must contain = {7}"
    )
    public static Collection combinations() {
        return Arrays.asList(new Object[][]{
                {"[1.0,2.0]", true, "1.0", true, "2.0", true, "1.0", true},
                {"(1.0,2.0)", true, "1.0", false, "2.0", false, "2.0", false},
                {"[1.0,2.0)", true, "1.0", true, "2.0", false, "1.0", true},
                {"(1.0,2.0]", true, "1.0", false, "2.0", true, "1.0", false},
                {"(,2.0]", true, "", false, "2.0", true, "1.0", true},
                {"[1.0,)", true, "1.0", true, "", false, "2.0", true},
                {"(,)", true, "", false, "", false, "any", true},
                {"1.0", false, "", false, "", false, "any", false},
                {"abc", false, "", false, "", false, null, false},
                {null, false, "", false, "", false, "any", false},
                {"1.0,2.0", false, "", false, "", false, "any", false}
        });
    }

    @Test
    public void testIsValid() {
        assertEquals(isValid, new VersionRange(range).isValid());
    }

    @Test
    public void testStartVersion() {
        assertEquals(startVersion, new VersionRange(range).getStartVersion());
    }

    @Test
    public void testEndVersion() {
        assertEquals(endVersion, new VersionRange(range).getEndVersion());
    }

    @Test
    public void testStartVersionIncluded() {
        assertEquals(isStartVersionIncluded, new VersionRange(range).isStartVersionIncluded());
    }

    @Test
    public void testEndVersionIncluded() {
        assertEquals(isEndVersionIncluded, new VersionRange(range).isEndVersionIncluded());
    }

    @Test
    public void testContains() {
        assertEquals(contains, new VersionRange(range).contains(testVersion));
    }

}
