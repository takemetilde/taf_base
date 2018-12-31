package com.taf.auto.jira.pojo;

import com.taf.auto.jira.IssueHandle;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/17/2017.
 */
public class IssueResponse {
    @JsonProperty("testExecIssue")
    public SelfKeyId testExecIssue;

    /**
     * Creates a handle from {@link SelfKeyId#self} and {@link SelfKeyId#key}.
     * @return the handle
     */
    public IssueHandle peekHandle() {
        return new IssueHandle(testExecIssue.self, testExecIssue.key);
    }
}
