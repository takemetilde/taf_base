package com.taf.auto.cucumber.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CucumberStepResult {
    @JsonProperty
    public long duration;

    @JsonProperty
    public String error_message;

    @JsonProperty
    public String status;
}
