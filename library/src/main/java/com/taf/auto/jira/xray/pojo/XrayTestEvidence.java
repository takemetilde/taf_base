package com.taf.auto.jira.xray.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/25/2017.
 */
public class XrayTestEvidence {
    @JsonProperty
    public String data;

    @JsonProperty
    public String filename;

    @JsonProperty
    public String contentType;
}
