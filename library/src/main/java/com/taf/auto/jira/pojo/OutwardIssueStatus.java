package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class OutwardIssueStatus {
    @JsonProperty
    public String self;

    @JsonProperty
    public String description;

    @JsonProperty
    public String iconUrl;

    @JsonProperty
    public String name;

    @JsonProperty
    public String id;

    @JsonProperty
    public OutwardIssueStatusCategory statusCategory;
}
