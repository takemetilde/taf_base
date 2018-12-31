package com.taf.auto.jira.xray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link XrayTestStatuses}.
 *
 * @author AF04261 mmorton
 */
public class XrayTestStatusesTest {
    @Test
    public void testNames() {
        assertEquals("Open", XrayTestStatuses.Open.toString());
        assertEquals("In Progress", XrayTestStatuses.InProgress.toString());
        assertEquals("Test Ready", XrayTestStatuses.TestReady.toString());
    }
}
