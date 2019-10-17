package com.taf.auto.jira.pojo.xray;

import com.taf.auto.jira.pojo.IssueType;
import com.taf.auto.jira.pojo.SelfValueId;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for the fields of a {@link XrayPreCondition}.
 *
 */
public class XrayPreConditionFields extends XrayFields {
    @JsonProperty(value = "customfield_14129")
    public SelfValueId preConType;

    @JsonProperty(value = "customfield_14130")
    public String conditions;

    /** only called via reflection */
    public XrayPreConditionFields() {
    }

    public XrayPreConditionFields(String assignee, String projectKey, String summary, String[] labels) {
        super(assignee, projectKey, summary, new IssueType("Pre-Condition"), labels);
        preConType = new SelfValueId("Cucumber");
    }
}
