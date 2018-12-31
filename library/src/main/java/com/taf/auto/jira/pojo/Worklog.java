package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class Worklog {
    @JsonProperty
    public int startAt;

    @JsonProperty
    public int maxResults;

    @JsonProperty
    public int total;

    @JsonProperty
    public String[] worklogs;
}
