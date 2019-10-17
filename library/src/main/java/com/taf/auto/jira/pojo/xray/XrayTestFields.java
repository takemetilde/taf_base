package com.taf.auto.jira.pojo.xray;

import com.taf.auto.jira.IssueTypes;
import com.taf.auto.jira.pojo.IssueType;
import com.taf.auto.jira.pojo.SelfValueId;

/**
 * POJO for the fields of a {@link XrayTest}.
 *
 */
public class XrayTestFields extends XrayFields {
    public XrayTestFields() {
        /** only called via reflection */
    }

    public XrayTestFields(String assignee, String projectKey, String summary, String[] labels, String cucumberScenario, String scenarioType) {
        super(assignee, projectKey, summary, new IssueType(IssueTypes.Test), labels);
        testType = new SelfValueId("Cucumber");
        cucumberTestType = new SelfValueId(scenarioType);
        this.cucumberScenario = cucumberScenario;
    }
}
