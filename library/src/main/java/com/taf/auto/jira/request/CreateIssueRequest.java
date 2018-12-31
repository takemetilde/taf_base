package com.taf.auto.jira.request;

import com.taf.auto.jira.pojo.Fields;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Abstract superclass for JIRA create Issue requests.
 */
public abstract class CreateIssueRequest<F extends Fields> extends AbstractRequest {
    @JsonProperty
    public F fields;

    protected CreateIssueRequest(F fields) {
        this.fields = fields;
    }
}
