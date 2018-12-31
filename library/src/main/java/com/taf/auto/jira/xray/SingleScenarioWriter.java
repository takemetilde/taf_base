package com.taf.auto.jira.xray;

import com.taf.auto.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by AD96317 on 8/17/2016.
 */
public class SingleScenarioWriter {
    private static final Logger LOG = LoggerFactory.getLogger(SingleScenarioWriter.class);

    public static String formatScenarioContent(String jiraTestTaskId, ScenarioForXray scenario) throws Exception {
        StringBuilder sb = new StringBuilder();

        try {
            writeFeatureLine(sb, scenario);
            writeBlankLine(sb);
            writeTagsLine(sb, scenario, jiraTestTaskId);
            writeScenarioHeader(sb, scenario);
            writeContent(sb, scenario);
        } catch(Exception e) {
            throw new Exception("Failed to format Scenario content", e);
        }

        return sb.toString();
    }

    public static void writeSingleScenarioToFeatureFile(File outputFile, String scenarioContent) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputFile);
            writer.write(scenarioContent);
        } catch (FileNotFoundException e) {
            LOG.error(String.format("Could not write to file (%s)", outputFile.getAbsolutePath()), e);
        } finally {
            IOUtil.safeClose(writer);
        }
    }

    private static void writeBlankLine(StringBuilder sb) {
        sb.append("\n");
    }

    private static void writeScenarioHeader(StringBuilder sb, ScenarioForXray scenario) {
        sb.append("\t").append(scenario.getType()).append(scenario.getScenarioName()).append("\n");
    }

    private static void writeContent(StringBuilder sb, ScenarioForXray scenario) {
        sb.append(TabGenerator.addTabsToEachLineOfString(scenario.getContent(), 2));
    }

    private static void writeTagsLine(StringBuilder sb, ScenarioForXray scenario, String jiraTestTaskId) {
        sb.append("\t@").append(jiraTestTaskId);

        for(String tag : scenario.getTags()) {
            sb.append(" @").append(tag);
        }

        sb.append("\n");
    }

    private static void writeFeatureLine(StringBuilder sb, ScenarioForXray scenario) {
        sb.append("Feature: ")
                .append(scenario.getFeatureName())
                .append("\n");
    }

    public static void main(String[] args) throws IOException {
        String projectKey = "ANREIMAGED";
        FeatureFileReader reader = new FeatureFileReader(projectKey);
        FeatureFile featureFile = reader.loadFromFile("C:\\Users\\AD96317\\Documents\\Workspace\\scripts\\src\\test\\resources\\com\\anthem\\portal\\claims\\EOB\\Dropdown.feature");
        //List<ScenarioForXray> scenariosForXray = FeatureFileSplitter.splitFeatureFile(featureFile, projectKey);

//        SingleScenarioWriter writer = new SingleScenarioWriter();
//
//        for (ScenarioForXray scenarioForXray : scenariosForXray) {
//            writer.writeSingleScenarioToFeatureFile(new File(), "ANRI-Test", scenarioForXray);
//        }
    }
}
