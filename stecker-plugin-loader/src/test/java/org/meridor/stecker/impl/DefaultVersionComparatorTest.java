package org.meridor.stecker.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.stecker.VersionRelation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class DefaultVersionComparatorTest {

    private final Optional<String> required;

    private final Optional<String> actual;

    private final VersionRelation relation;

    public DefaultVersionComparatorTest(String required, String actual, VersionRelation relation) {
        this.required = Optional.ofNullable(required);
        this.actual = Optional.ofNullable(actual);
        this.relation = relation;
    }

    @Parameterized.Parameters(name = "{1} must relate as {2} to {0}")
    public static Collection combinations() {
        return Arrays.asList(new Object[][]{
                {"1.0", "1.0", VersionRelation.EQUAL},
                {"any", null, VersionRelation.NOT_EQUAL},
                {"any", "", VersionRelation.NOT_EQUAL},
                {"1.0", "1.1", VersionRelation.GREATER_THAN},
                {"1.1", "1.0", VersionRelation.LESS_THAN},
                {"1.0", "0.9", VersionRelation.LESS_THAN},
                {"1.0", "1.1", VersionRelation.GREATER_THAN},
                {"0.9", "1.0", VersionRelation.GREATER_THAN},
                {null, "any", VersionRelation.IN_RANGE},
                {"", "any", VersionRelation.IN_RANGE},
                {"[1.0,2.0]", "1.1", VersionRelation.IN_RANGE},
                {"[1.0,)", "1.1", VersionRelation.IN_RANGE},
                {"[1.0,1.1]", "1.1", VersionRelation.IN_RANGE},
                {"(,1.1]", "1.1", VersionRelation.IN_RANGE},
                {"(,)", "1.1", VersionRelation.IN_RANGE},
                {"[1.0,1.1)", "1.1", VersionRelation.NOT_IN_RANGE},
                {"(1.0,1.1)", "1.0", VersionRelation.NOT_IN_RANGE}
        });
    }

    @Test
    public void testCompare() {
        assertThat(new DefaultVersionComparator().compare(required, actual), equalTo(relation));
    }
}
