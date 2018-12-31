package com.taf.auto.jira.xray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AD96317 on 6/1/2016.
 */
public class FeatureFile {
    private String originalFileName;
    private String nameLine;
    private final String projectKey;
    private String tagsLine;
    private Scenario background;
    private ArrayList<Scenario> scenarios;

    /**
     * @param nameLine       The line that includes <code>Feature: &lt;Feature_Name&gt;</code> where Feature_Name is the name of the feature.
     * @param topCommentLine This is the comment at the very top of the feature file. Some older feature files have the story
     *                       JIRA id in the comment instead of a tag. Can be null.
     * @param tagsLine       The line that includes the tags for an entire feature file. Can be null.
     * @param projectKey     the name of the JIRA projectKey the feature belongs to
     */
    public FeatureFile(String nameLine, String topCommentLine, String tagsLine, String projectKey) {
        this.nameLine = nameLine;
        this.projectKey = projectKey;
        this.scenarios = new ArrayList<>();
        this.tagsLine = extractTagFromTopCommentLine(topCommentLine);
        if(null != tagsLine) {
            this.tagsLine += tagsLine;
        }
    }

    private String extractTagFromTopCommentLine(String topCommentLine) {
        if (topCommentLine == null) return "";

        StringBuilder sb = new StringBuilder();
        String[] split = topCommentLine.replace("#", "").split(" ");
        for (String comment : split) {
            if (comment.contains(projectKey)) {
                sb.append("@").append(comment).append(" ");
            }
        }

        return sb.toString();
    }

    public Scenario getBackground() {
        return background;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void appendScenario(Scenario scenario) {
        if (scenario.isBackgroundScenario()) {
            background = scenario;
        } else {
            scenarios.add(scenario);
        }
    }

    public String getName() {
        String[] split = nameLine.split(":");

        return (split.length > 1) ? split[1] : "";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(originalFileName).append("\n");
        sb.append(nameLine).append("\n\n");
        for(Scenario scenario : scenarios) {
            sb.append(scenario).append("\n");
        }

        return sb.toString();
    }

    public List<Scenario> getScenarios() {
        return new ArrayList<>(scenarios);
    }

    public String getTagsLine() {
        return tagsLine;
    }

    public String getNameLine() {
        return nameLine;
    }

}
