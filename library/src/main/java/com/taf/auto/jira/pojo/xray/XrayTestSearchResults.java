package com.taf.auto.jira.pojo.xray;

import com.taf.auto.jira.pojo.AbstractSearchResults;

/**
 * Created by AF04261 on 1/10/2017.
 */
public class XrayTestSearchResults extends AbstractSearchResults<XrayTest> {
    @Override
    public Class<XrayTest> peekIssueConcreteClass() {
        return XrayTest.class;
    }
}
