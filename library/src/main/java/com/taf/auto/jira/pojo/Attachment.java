package com.taf.auto.jira.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 5/25/2017.
 */
public class Attachment extends SparseJsonPojo {
    @JsonProperty
    public String id;

    @JsonProperty
    public String filename;

    @JsonProperty
    public String content;
}
