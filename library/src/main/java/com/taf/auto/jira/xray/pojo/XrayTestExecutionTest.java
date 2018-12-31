package com.taf.auto.jira.xray.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO that holds information about a all Test Runs obtained from a Test Execution.
 *
 * @author AF04261 mmorton
 */
public class XrayTestExecutionTest {
    @JsonProperty
    public int id;

    @JsonProperty
    public String key;

    @JsonProperty
    public int rank;

    @JsonProperty
    public String status;
}
