package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class SelfKeyId extends SelfId {
    @JsonProperty
    public String key;

    public SelfKeyId() {
    }

    public SelfKeyId(String id, String key, String self) {
        this.id = id;
        this.key = key;
        this.self = self;
    }
}
