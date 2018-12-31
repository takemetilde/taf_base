package com.taf.auto.jira.xray.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for a Test Run returned by Xray's REST API.
 * ?
 *
 * @author AF04261
 */
public class XrayTestRun extends SparseJsonPojo {
    @JsonProperty
    public int id;

    @JsonProperty
    public String status;

    @JsonProperty
    public String testKey;

    @JsonProperty
    public String testExecKey;

    @JsonProperty
    public String startedOn;

    @JsonProperty
    public String finishedOn;

    @JsonProperty
    public String[] testEnvironments;
}
