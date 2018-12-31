package com.taf.auto.jira.pojo.xray;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * POJO holding the steps for a manual test. Not used for Cucumber.
 */
public class XrayTestSteps {
    @JsonIgnore
    public Object[] steps;
}
