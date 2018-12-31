package com.taf.auto;

import org.junit.Test;

import static com.taf.auto.IOUtil.NL;
import static com.taf.auto.StringUtil.allLinesEqual;
import static com.taf.auto.StringUtil.stripQuoted;
import static com.taf.auto.StringUtil.stripTrailingIndex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link StringUtil}.
 *
 * @author AF04261 mmorton
 */
public class StringUtilTest {
    @Test
    public void allLinesEqualTest() {
        assertTrue(allLinesEqual(null, null));
        assertFalse(allLinesEqual(null, ""));
        assertFalse(allLinesEqual("", null));

        String a = "alpha";
        assertFalse(allLinesEqual(a, ""));

        String b = "alpha" + NL + "beta";
        assertFalse(allLinesEqual(a, b));

        assertTrue(allLinesEqual(b, b));
    }

    @Test
    public void stripTrailingIndexTest() {
        assertEquals(null, stripTrailingIndex(null));
        assertEquals("DEV", stripTrailingIndex("DEV"));
        assertEquals("DEV", stripTrailingIndex("DEV1"));
        assertEquals("SIT", stripTrailingIndex("SIT22"));
    }

    @Test
    public void stripQuotedTest() {
        assertEquals(" has a benefit with a copay max          ", stripQuoted("| has a benefit with a copay max          |", "|"));
    }
}
