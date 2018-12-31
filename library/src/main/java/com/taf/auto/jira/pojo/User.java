package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 8/18/2016.
 */
public class User {
    @JsonProperty
    public String self;

    @JsonProperty
    public String name;

    @JsonProperty
    public String key;

    @JsonProperty
    public String emailAddress;

    @JsonProperty
    public AvatarUrls avatarUrls;

    @JsonProperty
    public String displayName;

    @JsonProperty
    public boolean active;

    @JsonProperty
    public String timeZone;

    public User() {}

    public User(String name) {
        this.name = name;
    }
}
