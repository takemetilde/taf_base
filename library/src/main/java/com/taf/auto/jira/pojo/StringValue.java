package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 2/24/2017.
 */
public class StringValue {
    @JsonProperty
    public String value;

    public StringValue() {

    }

    public StringValue(String value) {
        this.value = value;
    }
}
