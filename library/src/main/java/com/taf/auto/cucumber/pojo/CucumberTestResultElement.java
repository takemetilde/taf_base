package com.taf.auto.cucumber.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/25/2017.
 */
public class CucumberTestResultElement extends AbstractCucumberResult {
    @JsonIgnore
    public Object[] before;

    @JsonIgnore
    public Object[] after;

    @JsonProperty
    public String type;

    @JsonProperty
    public CucumberTestResultStep[] steps;

    @JsonProperty
    public CucumberTestResultTag[] tags;
}
