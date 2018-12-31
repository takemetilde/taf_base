package com.taf.auto.jira.xray.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/25/2017.
 */
public class XrayExecutionStep {
    @JsonProperty
    public String status;

    @JsonProperty
    public String comment;

    @JsonProperty
    public XrayTestEvidence[] evidences;
}
