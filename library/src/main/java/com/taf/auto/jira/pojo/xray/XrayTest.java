package com.taf.auto.jira.pojo.xray;

import com.taf.auto.jira.pojo.AbstractIssue;

import java.io.IOException;

import static com.taf.auto.IOUtil.NL;
import static com.taf.auto.io.JSONUtil.deepClone;

/**
 * Created by AF04261 on 1/10/2017.
 */
public class XrayTest extends AbstractIssue<XrayTestFields> {
    public XrayTest cloneWithScenario(String cucumberScenario) throws IOException {
        XrayTest clone = deepClone(this);
        clone.fields.cucumberScenario = cucumberScenario;
        return clone;
    }

    public XrayTest combineWith(XrayPreCondition user) throws IOException {
        String combinedScenario = user.fields.conditions + NL + fields.cucumberScenario;
        return cloneWithScenario(combinedScenario);
    }
}
