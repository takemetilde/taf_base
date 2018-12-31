package com.taf.auto.jira.pojo.xray;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 2/11/2017.
 */
public class XrayTestSetStatus {
    @JsonProperty
    public int id;

    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    public boolean isFinal;

    @JsonProperty
    public String color;

    @JsonProperty
    public boolean isNative;

    @JsonProperty
    public int statusCount;

    @JsonProperty
    public double statusPercent;
}
