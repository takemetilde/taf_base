package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class AvatarUrls {
    @JsonProperty(value = "48x48")
    public String x48;

    @JsonProperty(value = "24x24")
    public String x24;

    @JsonProperty(value = "16x16")
    public String x16;

    @JsonProperty(value = "32x32")
    public String x32;
}
