package com.taf.auto.jira;

/**
 * Created by AF04261 on 7/20/2017.
 */
public enum IssueWorkflowStatus {
    Reopened, Closed;

    public boolean matches(String o) {
        return name().equalsIgnoreCase((o));
    }
}
