package ru.meridor.stecker.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
        assertThat(new VersionRange(range).isValid(), equalTo(isValid));
    }

    @Test
    public void testStartVersion() {
        assertThat(new VersionRange(range).getStartVersion(), equalTo(startVersion));
    }

    @Test
    public void testEndVersion() {
        assertThat(new VersionRange(range).getEndVersion(), equalTo(endVersion));
    }

    @Test
    public void testStartVersionIncluded() {
        assertThat(new VersionRange(range).isStartVersionIncluded(), equalTo(isStartVersionIncluded));
    }

    @Test
    public void testEndVersionIncluded() {
        assertThat(new VersionRange(range).isEndVersionIncluded(), equalTo(isEndVersionIncluded));
    }

    @Test
    public void testContains() {
        assertThat(new VersionRange(range).contains(testVersion), equalTo(contains));
    }

}
