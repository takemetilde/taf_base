package com.taf.auto.jira.pojo;

import com.taf.auto.jira.IssueTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 8/16/2016.
 */
public class IssueType extends SelfId {
    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    public String iconUrl;

    @JsonProperty
    public boolean subtask;

    @JsonIgnore
    public Object avatarId;

    public IssueType() {
    }

    public IssueType(IssueTypes type) {
        this(type.toString());
    }

    public IssueType(String name) {
        this.name = name;
    }
}
