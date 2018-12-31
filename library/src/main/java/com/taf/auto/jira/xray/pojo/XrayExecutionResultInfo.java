package com.taf.auto.jira.xray.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/25/2017.
 */
public class XrayExecutionResultInfo {
    @JsonProperty
    public String summary;

    @JsonProperty
    public String description;

    @JsonProperty
    public String version;

    @JsonProperty
    public String user;

    @JsonProperty
    public String revision;

    @JsonProperty
    public String startDate;

    @JsonProperty
    public String finishDate;
}
