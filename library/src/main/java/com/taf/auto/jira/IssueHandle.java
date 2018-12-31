package com.taf.auto.jira;

import com.taf.auto.jira.pojo.AbstractIssue;

/**
 * Encapsulates the JIRA self link and key.
 *
 * @author AF04261 mmorton
 */
public final class IssueHandle {
    private final String self;
    private final String key;

    public IssueHandle(String key) {
        this(JIRAUtil.formatIssueURI(key), key);
    }

    public IssueHandle(String self, String key) {
        this.self = self;
        this.key = key;
    }

    public IssueHandle(AbstractIssue issue) {
        this(issue.self, issue.key);
    }

    public String getSelf() {
        return self;
    }

    public String getKey() {
        return key;
    }
}
