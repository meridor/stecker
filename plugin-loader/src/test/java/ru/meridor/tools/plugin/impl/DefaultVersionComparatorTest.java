package ru.meridor.tools.plugin.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.meridor.tools.plugin.VersionRelation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static ru.meridor.tools.plugin.VersionRelation.*;

@RunWith(Parameterized.class)
public class DefaultVersionComparatorTest {

    private final Optional<String> required;

    private final Optional<String> actual;

    private final VersionRelation relation;

    @Parameterized.Parameters(name = "{1} must relate as {2} to {0}")
    public static Collection combinations() {
        return Arrays.asList(new Object[][]{
                {"1.0", "1.0", EQUAL},
                {"any", null, NOT_EQUAL},
                {"any", "", NOT_EQUAL},
                {"1.0", "1.1", GREATER_THAN},
                {"1.1", "1.0", LESS_THAN},
                {"1.0", "0.9", LESS_THAN},
                {"1.0", "1.1", GREATER_THAN},
                {"0.9", "1.0", GREATER_THAN},
                {null, "any", IN_RANGE},
                {"", "any", IN_RANGE},
                {"[1.0,2.0]", "1.1", IN_RANGE},
                {"[1.0,)", "1.1", IN_RANGE},
                {"[1.0,1.1]", "1.1", IN_RANGE},
                {"(,1.1]", "1.1", IN_RANGE},
                {"(,)", "1.1", IN_RANGE},
                {"[1.0,1.1)", "1.1", NOT_IN_RANGE},
                {"(1.0,1.1)", "1.0", NOT_IN_RANGE}
        });
    }

    public DefaultVersionComparatorTest(String required, String actual, VersionRelation relation) {
        this.required = Optional.ofNullable(required);
        this.actual = Optional.ofNullable(actual);
        this.relation = relation;
    }

    @Test
    public void testCompare() {
        assertEquals(relation, new DefaultVersionComparator().compare(required, actual));
    }
}
