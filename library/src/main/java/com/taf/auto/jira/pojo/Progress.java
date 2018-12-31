package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class Progress {
    @JsonProperty
    public int progress;

    @JsonProperty
    public int total;

    @JsonIgnore
    public double percent;
}
