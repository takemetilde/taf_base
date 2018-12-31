package com.taf.auto.jira.pojo.xray;

import com.taf.auto.jira.pojo.Fields;
import com.taf.auto.jira.pojo.IssueType;
import com.taf.auto.jira.pojo.SelfValueId;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/10/2017.
 */
public abstract class XrayFields extends Fields {
    @JsonProperty(value = "customfield_14120")
    public SelfValueId testType;

    @JsonProperty(value = "customfield_14121")
    public SelfValueId cucumberTestType;

    @JsonProperty(value = "customfield_14122")
    public String cucumberScenario;

    @JsonProperty(value = "customfield_11224")
    public String[] sprints;

    /** The Test Set(s) this Test belongs to */
    @JsonProperty(value = "customfield_14126")
    public String[] testSetKeys;

    /** The Test Plan(s) this Test belongs to */
    @JsonProperty(value = "customfield_15020")
    public String[] testPlanKeys;

    @JsonProperty(value = "customfield_14127")
    public String[] preConditions;

    /** only called via reflection */
    public XrayFields() {
    }

    public XrayFields(String assignee, String projectKey, String summary, IssueType issueType, String[] labels) {
        super(assignee, projectKey, summary, issueType, labels);
    }
}
