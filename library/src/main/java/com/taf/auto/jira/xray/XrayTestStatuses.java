package com.taf.auto.jira.xray;

import com.taf.auto.jira.pojo.IssueStatus;

/**
 * Codifies the various status values for an Xray Test.
 *
 * @author AF04261 mmorton
 */
public enum XrayTestStatuses {
    Open(1),
    Reopened(1901, 4),
    Ready(1061, 11131),
    InProgress(4),
    IntegrationTestPrep(1051, 11127),
    TestReady(1071, 11128),
    Testing(1081, 11129),
    InReview(1041, 10046),
    ReadyForRelease(1091, 11130),
    Resolved(5),
    Closed(2, 6);

    public final IssueStatus status;

    private final String otherId;

    XrayTestStatuses(int id) {
        this(id, id);
    }

    XrayTestStatuses(int id, int otherId) {
        status = new IssueStatus();
        status.id = Integer.toString(id);
        this.otherId = Integer.toString(otherId);
    }

    public String toString() {
        String raw = name();
        StringBuilder name = new StringBuilder();
        name.append(raw.charAt(0));
        for(int i = 1, len = raw.length(); i < len; i++) {
            char ch = raw.charAt(i);
            if(Character.isUpperCase(ch)) {
                name.append(' ');
            }
            name.append(ch);
        }
        return name.toString();
    }

    public static XrayTestStatuses peek(IssueStatus status) {
        String idToMatch = status.id;
        for(XrayTestStatuses knownStatus : values()) {
            if(knownStatus.status.id.equals(idToMatch) || knownStatus.otherId.equals(idToMatch)) {
                return knownStatus;
            }
        }
        throw new IndexOutOfBoundsException("Status not known for ID: " + status.id);
    }
}
