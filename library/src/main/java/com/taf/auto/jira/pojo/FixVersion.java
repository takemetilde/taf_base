package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/23/2016.
 */
public class FixVersion {
    @JsonProperty
    public String self;

    @JsonProperty
    public String id;

    @JsonProperty
    public String description;

    @JsonProperty
    public String name;

    @JsonProperty
    public boolean archived;

    @JsonProperty
    public boolean released;

    @JsonProperty
    public String releaseDate;
}
