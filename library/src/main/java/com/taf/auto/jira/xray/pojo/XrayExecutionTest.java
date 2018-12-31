package com.taf.auto.jira.xray.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/25/2017.
 */
public class XrayExecutionTest {
    public interface Status {
        String PASS = "PASS";
        String FAIL = "FAIL";
        String ABORTED = "ABORTED";
    }

    @JsonProperty
    public String testKey;

    @JsonProperty
    public String start;

    @JsonProperty
    public String finish;

    @JsonProperty
    public String comment;

    @JsonProperty
    public String status;

    @JsonIgnore
    public Object evidences;

    @JsonProperty
    public XrayExecutionStep[] steps;

    @JsonProperty
    public String[] examples;

    @JsonProperty
    public String[] defects;
}
