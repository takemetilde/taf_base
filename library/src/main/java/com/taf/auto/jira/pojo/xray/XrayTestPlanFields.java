package com.taf.auto.jira.pojo.xray;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for the fields of a {@link XrayTestPlan}.
 *
 */
public class XrayTestPlanFields extends XrayFields implements ContainsTests {
    @JsonProperty(value = "customfield_14726")
    public String[] tests;

    @Override
    public String[] peekTests() {
        return tests;
    }
}
