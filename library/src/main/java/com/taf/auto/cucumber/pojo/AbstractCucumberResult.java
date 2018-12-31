package com.taf.auto.cucumber.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/25/2017.
 */
abstract class AbstractCucumberResult extends SparseJsonPojo {
    @JsonProperty
    public int line;

    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    public String id;

    @JsonProperty
    public String keyword;
}
