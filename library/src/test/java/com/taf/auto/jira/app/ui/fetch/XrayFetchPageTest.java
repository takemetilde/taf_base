package com.taf.auto.jira.app.ui.fetch;

import org.junit.Test;

import static com.taf.auto.jira.app.ui.fetch.XrayFetchPage.splitKeys;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by AF04261 on 3/10/2017.
 */
public class XrayFetchPageTest {
    @Test
    public void splitKeysTest() {
        assertArrayEquals(new String[0], splitKeys(""));
        assertArrayEquals(new String[0], splitKeys(","));
        assertArrayEquals(new String[] { "FOO-12" }, splitKeys("FOO-12"));
        assertArrayEquals(new String[] { "FOO-12" }, splitKeys(",FOO-12"));
        assertArrayEquals(new String[] { "FOO-12", "BAR-13" }, splitKeys("FOO-12, BAR-13"));
        assertArrayEquals(new String[] { "FOO-12", "BAR-13" }, splitKeys("FOO-12 BAR-13"));
        assertArrayEquals(new String[] { "FOO-12", "BAR-13" }, splitKeys("FOO-12; BAR-13"));
    }
}
