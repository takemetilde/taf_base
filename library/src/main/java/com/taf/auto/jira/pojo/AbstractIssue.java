package com.taf.auto.jira.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/10/2017.
 */
public abstract class AbstractIssue<F extends Fields> extends SparseJsonPojo {
    @JsonProperty
    public String expand;

    @JsonProperty
    public String id;

    @JsonProperty
    public String self;

    @JsonProperty
    public String key;

    @JsonProperty
    public F fields;
}
