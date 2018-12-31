package com.taf.auto.jira.xray;

import com.taf.auto.common.PrettyPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AD96317 on 6/1/2016.
 */
public class Scenario {
    private String nameLine;
    private String tagsLine;
    private List<String> contentLines;

    /**
     * Default constructor.
     * 
     * @param nameLine This line includes Scenario: &lt;Scenario_Name&gt; where Scenario_Name is the optional name of the Scenario.
     * @param tagsLine The line that includes the tags for the scenario being created. Can be null.
     */
    public Scenario(String nameLine, String tagsLine) {
        this.nameLine = nameLine;
        this.tagsLine = tagsLine;
        this.contentLines = new ArrayList<>();
    }

    public boolean isBackgroundScenario() {
        return nameLine.contains("Background");
    }

    public void appendContentLine(String contentLine) {
        contentLines.add(contentLine);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(tagsLine).append("\n");
        sb.append(nameLine).append("\n");
        sb.append(getContent());

        return sb.toString();
    }

    public String getTagsLine() {
        return tagsLine;
    }

    public String getNameLine() {
        return nameLine;
    }

    public List<String> getContentLines() {
        return new ArrayList<>(contentLines);
    }

    public String getContent() {
        return PrettyPrinter.prettyList(contentLines, "\n");
    }

    public String getType() {
        return nameLine.split(":")[0];
    }

    public String getName() {
        String[] split = nameLine.split(":");

        return (split.length > 1) ? split[1].trim() : "";
    }
}