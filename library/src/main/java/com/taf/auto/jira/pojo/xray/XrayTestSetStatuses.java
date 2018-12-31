package com.taf.auto.jira.pojo.xray;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 2/11/2017.
 */
public class XrayTestSetStatuses {
    @JsonProperty
    public int count;

    @JsonProperty
    public XrayTestSetStatus[] statuses;
}
