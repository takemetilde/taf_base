package com.taf.auto.jira.request;

import com.taf.auto.jira.ITTeams;
import com.taf.auto.jira.pojo.IssueLinks;
import com.taf.auto.jira.pojo.SelfValueId;
import com.taf.auto.jira.pojo.xray.XrayTestFields;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Optional;

/**
 * The transport to send to JIRA to create an Xray Test.
 */
public class CreateXrayTestRequest extends CreateIssueRequest<XrayTestFields> {
    @JsonProperty
    public IssueLinks update;

    private String[] additionalLinks;

    public CreateXrayTestRequest(String username, String projectKey, String summary, String[] labels, String[] links, String cucumberScenario, String scenarioType) {
        this(username, projectKey, summary, labels, links, cucumberScenario, scenarioType, Optional.empty());
    }

    public CreateXrayTestRequest(String username, String projectKey, String summary, String[] labels, String[] links, String cucumberScenario, String scenarioType, Optional<ITTeams> team) {
        super(new XrayTestFields(username, projectKey, summary, labels, cucumberScenario, scenarioType));
        team.ifPresent(t -> fields.IT_Team = new SelfValueId[] { new SelfValueId(Integer.toString(t.id))});

        int numLinks = links.length;
        String singleOrNoLink = numLinks == 0 ? null : links[0];
        if(numLinks > 1) {
            additionalLinks = Arrays.copyOfRange(links, 1, numLinks);

        } else {
            additionalLinks = new String[0];
        }

        update = new IssueLinks(singleOrNoLink, IssueLinks.LinkVerb.Tests);
    }

    /**
     * The issue creation query can only create a single link. Any link beyond the first must be handled in a followup.
     *
     * @return any additional links
     */
    public String[] getAdditionalLinks() {
        return additionalLinks;
    }
}
