package com.taf.auto.jira.xray.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO to import an Xray Test Execution.
 *
 * See http://confluence.xpand-addons.com/display/XRAY/Import+Execution+Results+-+REST.
 */
public class XrayExecutionResult {
    @JsonProperty
    public XrayExecutionResultInfo info;

    @JsonProperty
    public XrayExecutionTest[] tests;
}
