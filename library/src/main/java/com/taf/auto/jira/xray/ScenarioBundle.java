package com.taf.auto.jira.xray;

import com.taf.auto.jira.pojo.AbstractIssue;

import java.nio.file.Path;

/**
 * Composes key objects related to a scenario.
 *
 * @author AF04261 mmorton
 */
public final class ScenarioBundle<I extends AbstractIssue> {
    public final Path file;
    public final ScenarioForXray scenario;
    public I issue;

    public ScenarioBundle(Path file, ScenarioForXray scenario, I issue) {
        this.file = file;
        this.scenario = scenario;
        this.issue = issue;
    }

    public final String peekKey() {
        return issue.key;
    }

    public String toString() {
        return issue.key;
    }
}