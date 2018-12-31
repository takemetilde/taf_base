package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class IssueLinkType {
    @JsonProperty
    public String id;

    @JsonProperty
    public String name;

    @JsonProperty
    public String inward;

    @JsonProperty
    public String outward;

    @JsonProperty
    public String self;
}
