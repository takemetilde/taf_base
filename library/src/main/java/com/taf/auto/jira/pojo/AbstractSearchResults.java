package com.taf.auto.jira.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/10/2017.
 */
public abstract class AbstractSearchResults<I extends AbstractIssue> extends SparseJsonPojo {
    @JsonProperty
    public String expand;

    @JsonProperty
    public int startAt;

    @JsonProperty
    public int maxResults;

    @JsonProperty
    public int total;

    @JsonProperty
    public I[] issues;

    public abstract Class<I> peekIssueConcreteClass();
}
