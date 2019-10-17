package com.taf.auto.jira.pojo.xray;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for the fields of a {@link XrayTestSet}.
 *
 */
public class XrayTestSetFields extends XrayFields implements ContainsTests {
    @JsonProperty(value = "customfield_14131")
    public String[] tests;

    @JsonProperty
    public XrayTestSetStatuses customfield_14132;

    @JsonProperty
    public double customfield_14721;

    @Override
    public String[] peekTests() {
        return tests;
    }
}
