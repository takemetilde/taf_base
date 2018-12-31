package com.taf.auto.jira.pojo.xray;

import com.taf.auto.StringUtil;
import com.taf.auto.jira.xray.XrayUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.taf.auto.IOUtil.NL;
import static com.taf.auto.jira.ToolCreds.JIRA_PASS;
import static com.taf.auto.jira.ToolCreds.JIRA_USER;
import static com.taf.auto.jira.xray.ScenarioEmitter.emit;
import static com.taf.auto.jira.pojo.xray.TestXrayUtil.Keys.TEST;
import static com.taf.auto.jira.pojo.xray.TestXrayUtil.Keys.TEST_SET;
import static com.taf.auto.jira.xray.XrayUtil.fetchTestsByTestSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link XrayUtil}.
 */
public class TestXrayUtil {
    interface Keys {
        String TEST_SET = "ANREIMAGED-23356";
        String TEST = "ANREIMAGED-23358";
    }

    @Ignore("passes locally but fails when built from Bamboo") @Test
    public void fetchIssuesByTestSetTest() throws IOException {
        XrayTest[] issues = fetchTestsByTestSet(TEST_SET, JIRA_USER, JIRA_PASS);
        assertEquals(1, issues.length);
        XrayTest test = issues[0];
        assertEquals(TEST, test.key);

        String emit = emit(test, "xray-fetch");

        String expected =
                "Feature: Test for automation-base-library unit tests" + NL +
                "  @ANREIMAGED-23358 @xray-fetch" + NL +
                "  Scenario: Test for automation-base-library unit tests" + NL +
                "    Given I unlock the door" + NL +
                "    When I turn the handle" + NL +
                "    Then the door opens" + NL;
        boolean equal = StringUtil.allLinesEqual(expected, emit);
        assertTrue("Expected:\n" + expected + "\n\nActual:\n" + emit, equal);
    }
}
