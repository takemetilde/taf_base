package com.taf.auto.jira.xray;

import com.taf.auto.common.PrettyPrinter;
import com.taf.auto.jira.pojo.IssueLink;
import com.taf.auto.jira.pojo.xray.XrayTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the data needed to pass to JIRA to create a test case.
 *
 */
public final class ScenarioForXray {
    private static final Logger LOG = LoggerFactory.getLogger(ScenarioForXray.class);

    private final String featureName;
    private final String content;
    private final String scenarioName;
    private final String type;
    private String[] tags;
    private final String[] jiraIds;

    public ScenarioForXray(String content, String featureName, String scenarioName, String type, List<String> tags, String projectKey) {
        this(content, featureName, scenarioName, type, tags, getJiraIdsFromTags(tags, projectKey));
    }

    private ScenarioForXray(String content, String featureName, String scenarioName, String type, List<String> tags, String[] jiraIds) {
        this.content = content;
        if(null == scenarioName || scenarioName.trim().isEmpty()) {
            throw new InvalidScenarioException("scenarioName is missing");
        }
        if(scenarioName.length() > 256) {
            throw new InvalidScenarioException("scenarioName is too long and must be 256 characters at most.");
        }
        this.scenarioName = scenarioName;
        this.type = type;
        this.tags = tags.toArray(new String[tags.size()]);
        this.jiraIds = jiraIds;
        this.featureName = featureName;
    }

    public static ScenarioForXray resolve(XrayTest test) {
        String[] links;
        if(null != test.fields.issuelinks) {
            List<String> potentialLinks = new ArrayList<>(test.fields.issuelinks.length);
            for (IssueLink link : test.fields.issuelinks) {
                if(null != link.outwardIssue) {
                    potentialLinks.add(link.outwardIssue.key);
                } else {
                    LOG.warn("Link: {} does not have an outward issue.", link.id);
                }
            }
            links = potentialLinks.toArray(new String[potentialLinks.size()]);
        } else {
            links = new String[0];
        }
        return new ScenarioForXray(test.fields.cucumberScenario, test.fields.summary,
                test.fields.summary, test.fields.cucumberTestType.value,
                Arrays.asList(test.fields.labels), links);
    }

    public String getFeatureName() {
        StringBuilder name = new StringBuilder();
        if(jiraIds.length > 0)
            name.append(PrettyPrinter.prettyArray(jiraIds)).append(": ");
        name.append(featureName);
        return name.toString();
    }

    public String getContent() {
        return content;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public String getType() {
        return type;
    }

    public String[] getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        for(String t : tags) {
            if(t.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public void setTags(List<String> tags) {
        this.tags = tags.toArray(new String[tags.size()]);
    }

    public List<String> peekSortedTags() {
        List<String> sorted = new ArrayList<>(tags.length);
        for(String tag : tags)
            sorted.add(tag);
        Collections.sort(sorted);
        return sorted;
    }

    private static String[] getJiraIdsFromTags(List<String> tags, String projectKey) {
        List<String> jiraIds = new ArrayList<>();
        for (String tag : tags) {
            if (tag.contains(projectKey)) jiraIds.add(tag);
        }

        return jiraIds.toArray(new String[jiraIds.size()]);
    }

    public String[] getJiraIds() {
        return jiraIds;
    }

    public String[] peekJiraIds(String... exclusions) {
        List<String> screened = new ArrayList<>(jiraIds.length);
        for(String id : jiraIds) {
            boolean include = true;
            for(String exclusion : exclusions) {
                if(id.equalsIgnoreCase(exclusion)) {
                    LOG.info("Excluding: " + id);
                    include = false;
                    break;
                }
            }
            if(include) {
                screened.add(id);
            }
        }
        return screened.toArray(new String[screened.size()]);
    }

    public ScenarioForXray cloneWithDifferentTags(List<String> differentTags) {
        return new ScenarioForXray(content, featureName, scenarioName, type, differentTags, jiraIds);
    }

    public ScenarioForXray cloneWithTagsRemoved(List<String> tagsToRemove) {
        List<String> survivingTags = new ArrayList<>();
        for(String tag : tags) {
            if(!tagsToRemove.contains(tag)) {
                survivingTags.add(tag);
            }
        }
        return new ScenarioForXray(content, featureName, scenarioName, type, survivingTags, jiraIds);
    }

    public ScenarioForXray cloneWithJIRAIDs(String... ids) {
        return new ScenarioForXray(content, featureName, scenarioName, type, Arrays.asList(tags), ids);
    }

    public ScenarioForXray cloneWithUpdatedScenario(String content, String type) {
        return new ScenarioForXray(content, featureName, scenarioName, type, Arrays.asList(tags), jiraIds);
    }

    public ScenarioForXray cloneWithUpdatedScenario(String name, String content, String type) {
        return new ScenarioForXray(content, name, name, type, Arrays.asList(tags), jiraIds);
    }

    public static List<ScenarioForXray> parseScenarios(File file, String projectKey) throws Exception {
        FeatureFileReader reader = new FeatureFileReader(projectKey);
        FeatureFile origFeatureFile = reader.loadFromFile(file);

        List<ScenarioForXray> scenarios = FeatureFileSplitter.splitFeatureFile(origFeatureFile, projectKey);
        if(scenarios.isEmpty())
            throw new Exception("No Scenarios found in: " + file);
        return scenarios;
    }
}