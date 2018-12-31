package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/5/2017.
 */
public class Filter {
    @JsonProperty
    public String self;

    @JsonProperty
    public String id;

    @JsonProperty
    public String name;

    @JsonProperty
    public User owner;

    @JsonProperty
    public String jql;

    @JsonProperty
    public String viewUrl;

    @JsonProperty
    public String searchUrl;

    @JsonProperty
    public boolean favourite;

    @JsonIgnore
    public Object sharePermissions;

    @JsonIgnore
    public Object sharedUsers;

    @JsonIgnore
    public Object subscriptions;
}
