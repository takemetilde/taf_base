package com.taf.auto.jira.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 7/20/2017.
 */
public class Transition extends SparseJsonPojo {
    @JsonProperty
    public String id;

    @JsonProperty
    public String name;
}
