package com.taf.auto.cucumber.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/25/2017.
 */
public class CucumberStepMatch {
    @JsonIgnore
    public Object arguments;

    @JsonProperty
    public String location;
}
