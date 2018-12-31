package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class IssueStatusCategory extends SelfId {
    @JsonProperty
    public String key;

    @JsonProperty
    public String colorName;

    @JsonProperty
    public String name;
}
