package com.taf.auto.cucumber.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 6/22/2017.
 */
public class CucumberStepEmbeddings extends SparseJsonPojo {
    @JsonProperty
    public String data;

    @JsonProperty
    public String mime_type;
}
