package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/23/2016.
 */
public class Comment {
    @JsonProperty
    public String self;

    @JsonProperty
    public String id;

    @JsonProperty
    public User author;

    @JsonProperty
    public String body;

    @JsonProperty
    public User updateAuthor;

    @JsonProperty
    public String created;

    @JsonProperty
    public String updated;
}
