package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class Votes extends Self {
    @JsonProperty
    public int votes;

    @JsonProperty
    public boolean hasVoted;
}
