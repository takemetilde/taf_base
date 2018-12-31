package com.taf.auto.jira.request;

import com.taf.auto.jira.pojo.IssueLinks;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to add an additional Issue link.
 *
 * @author AF04261 mmorton
 */
public class LinkJIRAIssueRequest extends AbstractRequest{
    @JsonProperty
    public IssueLinks update;

    public LinkJIRAIssueRequest(String issueKey, IssueLinks.LinkVerb verb) {
        update = new IssueLinks(issueKey, verb);
    }
}
