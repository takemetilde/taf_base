package com.taf.auto.jira.request;

import com.taf.auto.jira.pojo.xray.XrayPreConditionFields;

/**
 * Created by AF04261 on 8/25/2017.
 */
public class CreateXrayPreConditionRequest extends CreateIssueRequest<XrayPreConditionFields> {
    public CreateXrayPreConditionRequest(String assignee, String projectKey, String summary, String[] labels) {
        super(new XrayPreConditionFields(assignee, projectKey, summary, labels));
    }
}
