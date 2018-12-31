package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class Project extends SelfId {
    @JsonProperty
    public String key;

    @JsonProperty
    public String name;

    @JsonProperty
    public AvatarUrls avatarUrls;

    @JsonProperty
    public ProjectCategory projectCategory;

    @JsonIgnore
    public String expand;

    @JsonIgnore
    public String projectTypeKey;

    public Project() {}

    public Project(String key) {
        this.key = key;
    }
}
