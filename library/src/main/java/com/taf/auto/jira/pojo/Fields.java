package com.taf.auto.jira.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 1/10/2017.
 */
public class Fields extends SparseJsonPojo {
    @JsonProperty
    public FixVersion[] fixVersions;

    @JsonProperty
    public String lastViewed;

    @JsonProperty
    public String[] labels;

    @JsonProperty
    public String aggregatetimeoriginalestimate;

    @JsonProperty
    public String timeestimate;

    @JsonProperty
    public FixVersion[] versions;

    @JsonProperty
    public Resolution resolution;

    @JsonProperty
    public IssueLink[] issuelinks;

    @JsonProperty
    public User assignee;

    @JsonProperty
    public IssueStatus status;

    @JsonProperty
    public Component[] components;

    @JsonProperty
    public String aggregatetimeestimate;

    @JsonProperty
    public User creator;

    @JsonIgnore
    public String[] subtasks;

    @JsonProperty
    public User reporter;

    @JsonProperty
    public Progress aggregateprogress;

    @JsonProperty
    public Progress progress;

    @JsonProperty
    public Votes votes;

    @JsonProperty
    public Worklog worklog;

    @JsonProperty
    public IssueType issuetype;

    @JsonProperty
    public String timespent;

    @JsonProperty
    public Project project;

    @JsonProperty
    public String aggregatetimespent;

    @JsonProperty
    public String resolutiondate;

    @JsonIgnore
    public int workratio;

    @JsonProperty
    public Watches watches;

    @JsonProperty
    public String created;

    @JsonProperty
    public String updated;

    @JsonProperty
    public String timeoriginalestimate;

    @JsonProperty
    public String description;

    @JsonProperty
    public Object timetracking;

    @JsonProperty
    public Attachment[] attachment;

    @JsonProperty
    public String summary;

    @JsonProperty
    public String environment;

    @JsonProperty
    public String duedate;

    @JsonProperty
    public Comments comment;

    @JsonProperty(value = "customfield_12479")
    public SelfValueId[] IT_Team;

    public Fields() {
        /** only called via reflection */
    }

    public Fields(String assignee, String projectKey, String summary, IssueType issueType, String[] labels) {
        project = new Project(projectKey);
        this.summary = summary;
        this.issuetype = issueType;
        this.labels = labels;
        this.assignee = null != assignee ? new User(assignee) : null;
    }
}
