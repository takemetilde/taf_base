package com.taf.auto.jira.pojo;

/**
 * Created by AF04261 on 1/5/2017.
 */
public class SearchResults extends AbstractSearchResults<Issue> {

    @Override
    public Class<Issue> peekIssueConcreteClass() {
        return Issue.class;
    }
}
