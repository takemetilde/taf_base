package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/23/2016.
 */
public class Component {
    @JsonProperty
    public String self;

    @JsonProperty
    public String id;

    @JsonProperty
    public String name;

    @JsonProperty
    public String description;
}
