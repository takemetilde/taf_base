package com.taf.auto.jira.xray.request;

import com.taf.auto.jira.pojo.StringValue;
import com.taf.auto.jira.request.AbstractRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 2/24/2017.
 */
public class XrayUpdateTestRequest extends AbstractRequest {

    public static class Fields {
        @JsonProperty(value = "customfield_14121")
        public StringValue cucumberTestType;

        @JsonProperty(value = "customfield_14122")
        public String cucumberScenario;

        @JsonProperty(value = "customfield_14127")
        public String[] preConditions;
    }

    @JsonProperty
    public Fields fields;

    public XrayUpdateTestRequest() {

    }

    public XrayUpdateTestRequest(String scenarioBody) {
        fields = new Fields();
        fields.cucumberScenario = scenarioBody;
    }

    public XrayUpdateTestRequest(String scenarioType, String scenarioBody) {
        this(scenarioBody);
        fields.cucumberTestType = new StringValue(scenarioType);
    }

    public XrayUpdateTestRequest(String scenarioType, String scenarioBody, String[] preConditions) {
        this(scenarioType, scenarioBody);
        fields.preConditions = preConditions;
    }
}
