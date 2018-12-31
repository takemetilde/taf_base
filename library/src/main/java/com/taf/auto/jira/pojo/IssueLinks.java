package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 8/17/2016.
 */
public class IssueLinks {
    public enum LinkVerb {
        Tests, Cloners
    }

    @JsonProperty
    public JIRAAddLink[] issuelinks;

    public IssueLinks(String issueLink, LinkVerb verb) {
        String[] issueLinks = null != issueLink ? new String[] {issueLink} : new String[0];
        issuelinks = new JIRAAddLink[issueLinks.length];
        for(int i = 0; i < issueLinks.length; i++) {
            issuelinks[i] = new JIRAAddLink(issueLinks[i], verb);
        }
    }

    public static class JIRAAddLink {
        @JsonProperty
        public JIRALinkBundle add;

        public JIRAAddLink(String key, LinkVerb verb) {
            add = new JIRALinkBundle(key, verb);
        }
    }

    public static class JIRALinkBundle {
        @JsonProperty
        public JIRALinkType type;

        @JsonProperty
        public JIRALinkIssue outwardIssue;

        public JIRALinkBundle(String key, LinkVerb verb) {
            type = new JIRALinkType(verb);
            outwardIssue = new JIRALinkIssue(key);
        }
    }

    public static class JIRALinkType {
        @JsonProperty
        public String name;

        public JIRALinkType(LinkVerb verb) {
            this.name = verb.name();
        }
    }

    public static class JIRALinkIssue {
        @JsonProperty
        public String key;

        public JIRALinkIssue(String key) {
            this.key = key;
        }
    }
}

