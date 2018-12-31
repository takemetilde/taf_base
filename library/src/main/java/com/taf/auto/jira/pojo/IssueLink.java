package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class IssueLink {
    @JsonProperty
    public String id;

    @JsonProperty
    public String self;

    @JsonProperty
    public IssueLinkType type;

    @JsonProperty
    public LinkedIssue inwardIssue;

    @JsonProperty
    public LinkedIssue outwardIssue;
}
