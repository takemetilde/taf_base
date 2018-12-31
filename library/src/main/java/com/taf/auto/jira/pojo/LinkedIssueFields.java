package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class LinkedIssueFields {
    @JsonProperty
    public String summary;

    @JsonProperty
    public OutwardIssueStatus status;

    @JsonIgnore
    public Object priority;

    @JsonProperty
    public OutwardIssueType issuetype;
}
