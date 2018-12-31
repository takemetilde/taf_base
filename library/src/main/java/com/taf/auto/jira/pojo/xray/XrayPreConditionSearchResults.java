package com.taf.auto.jira.pojo.xray;

import com.taf.auto.jira.pojo.AbstractSearchResults;

/**
 * Created by AF04261 on 4/5/2017.
 */
public class XrayPreConditionSearchResults extends AbstractSearchResults<XrayPreCondition> {
    @Override
    public Class<XrayPreCondition> peekIssueConcreteClass() {
        return XrayPreCondition.class;
    }
}
