package com.taf.auto.jira;

import com.taf.auto.jira.JIRAUtil.FixVersion;
import com.taf.auto.jira.JIRAUtil.Label;
import com.taf.auto.jira.pojo.IssueResponse;
import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static com.taf.auto.jira.ITTeams.NO_TEAM;
import static com.taf.auto.jira.JIRAUtilTest.TestConstants.*;
import static com.taf.auto.jira.ToolCreds.JIRA_CREDS;
import static com.taf.auto.rest.RestUtilTest.ResponseTest.assertResponse201;
import static com.taf.auto.rest.RestUtilTest.ResponseTest.assertResponse204;

/**
 * Unit tests for {@link JIRAUtil}.
 *
 * @author AF04261 mmorton
 */
public class JIRAUtilTest {
    public interface TestConstants {
        /** Handle for the Test Execution used during unit tests */
        IssueHandle TEST_EXECUTION = new IssueHandle("ANREIMAGED-10885");

        String SUMMARY = "This is a Test Summary";

        String ENV = "DEV";
        String REVISION = ENV;

        String LABEL_1 = "label-1";
        String LABEL_2 = "label-2";
        String LABEL_3 = "label-3";

        String[] ALL_LABELS = { LABEL_1, LABEL_2, LABEL_3 };

        String COMMENT_ADD = "This comment was added by " + JIRAUtilTest.class.getSimpleName();
        String COMMENT_EDIT = "This comment was edited by " + JIRAUtilTest.class.getSimpleName();

        String FIX_VERSION = "PI 9";
    }

    @Ignore @Test
    public void testUpdateSummary() {
        assertResponse204(JIRAUtil.Summary.update(TEST_EXECUTION, SUMMARY, JIRA_CREDS));
    }

    @Ignore @Test
    public void testUpdateEnv() {
        assertResponse204(JIRAUtil.Environment.update(TEST_EXECUTION, ENV, JIRA_CREDS));
    }

    @Ignore @Test
    public void testUpdateRevision() {
        assertResponse204(JIRAUtil.Revision.update(TEST_EXECUTION, REVISION, JIRA_CREDS));
    }

    @Ignore @Test
    public void testAddLabels() {
        assertResponse204(Label.add(TEST_EXECUTION, Arrays.asList(ALL_LABELS), JIRA_CREDS));
    }

    @Ignore @Test
    public void testManageComment() {
        Response response = JIRAUtil.Comment.add(TEST_EXECUTION, COMMENT_ADD, JIRA_CREDS);
        assertResponse201(response);
        String commentID = response.path("id");
        assertResponse204(JIRAUtil.Comment.edit(TEST_EXECUTION, commentID, COMMENT_EDIT, JIRA_CREDS));
        assertResponse204(JIRAUtil.Comment.remove(TEST_EXECUTION, commentID, JIRA_CREDS));
    }

    @Ignore @Test
    public void testUpdateIssueRemoveLabel() {
        assertResponse204(Label.remove(TEST_EXECUTION, LABEL_2, JIRA_CREDS));
    }

    @Ignore @Test
    public void testUpdateFixVersion() {
        assertResponse204(FixVersion.update(TEST_EXECUTION, FIX_VERSION, JIRA_CREDS));
    }

    @Ignore @Test
    public void testRemoveFixVersion() {
        assertResponse204(FixVersion.remove(TEST_EXECUTION, FIX_VERSION, JIRA_CREDS));
    }

    @Ignore @Test
    public void testUpdateITTeam() {
        assertResponse204(JIRAUtil.ITTeam.update(TEST_EXECUTION, new ITTeams(NO_TEAM, "DevOps"), JIRA_CREDS));
    }

    @Ignore @Test
    public void testUpdateSprint() {
        assertResponse204(JIRAUtil.ITTeam.update(TEST_EXECUTION, new ITTeams(NO_TEAM, "DevOps"), JIRA_CREDS));
    }
}
