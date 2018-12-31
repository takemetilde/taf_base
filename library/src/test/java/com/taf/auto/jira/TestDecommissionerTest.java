package com.taf.auto.jira;

import com.taf.auto.jira.xray.pojo.XrayTestRun;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.taf.auto.IOUtil.readBytesFromClasspath;
import static com.taf.auto.io.JSONUtil.decode;
import static com.taf.auto.jira.TestDecommissioner.detectEnvironments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TestDecommissioner}.
 */
public final class TestDecommissionerTest {
    private static XrayTestRun[] load(String name) throws IOException, URISyntaxException {
        return decode(readBytesFromClasspath("/jira/xray/" + name + ".json"), XrayTestRun[].class);
    }

    private static Set<String> toSet(String... elems) {
        Set<String> set = new LinkedHashSet<>();
        for(String elem : elems) {
            set.add(elem);
        }
        return set;
    }

    private static void validEnvs(XrayTestRun[] testRuns, String... envs) {
        assertEquals(toSet(envs), detectEnvironments(testRuns));
    }

    @Test
    public void shouldDecommissionTest() throws IOException, URISyntaxException {
        XrayTestRun[] testRuns = load("test_runs_14362");
        validEnvs(testRuns, "DEV2", "SIT1");
        assertTrue(TestDecommissioner.shouldDecommission(testRuns));
    }

    @Test
    public void shouldNotDecommissionTest() throws IOException, URISyntaxException {
        XrayTestRun[] testRuns = load("test_runs_14103");
        validEnvs(testRuns, "DEV2", "SIT1");
        assertFalse(TestDecommissioner.shouldDecommission(testRuns));
    }
}
