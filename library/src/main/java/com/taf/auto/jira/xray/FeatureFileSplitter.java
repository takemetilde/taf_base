package com.taf.auto.jira.xray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by AD96317 on 8/16/2016.
 */
public class FeatureFileSplitter {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureFileSplitter.class);

    public static List<ScenarioForXray> splitFeatureFile(@Nonnull FeatureFile featureFile, String projectKey) throws FeatureFileSyntaxException {
        HashSet<String> featureTags = getTagsFromTagsLine(featureFile.getTagsLine());

        List<ScenarioForXray> rtn = new ArrayList<>();
        for(Scenario scenario : featureFile.getScenarios()) {
            try {
                StringBuilder sb = new StringBuilder();
                HashSet<String> scenarioTags = getTagsFromTagsLine(scenario.getTagsLine());
                scenarioTags.addAll(featureTags);

                if (featureFile.getBackground() != null) {
                    sb.append(featureFile.getBackground().getContent()).append("\n");
                }

                sb.append(getContentWithNormalizedGivens(featureFile, scenario));

                ScenarioForXray toAdd = new ScenarioForXray(
                        sb.toString()
                        , featureFile.getName()
                        , scenario.getName()
                        , scenario.getType()
                        , getTagsWithoutAts(new ArrayList<>(scenarioTags))
                        , projectKey
                );

                rtn.add(toAdd);
            } catch (Exception e) {
                String msg = featureFile.getOriginalFileName() + " is not valid:\n> " + e.getMessage();
                LOG.error(msg);
                throw new FeatureFileSyntaxException(msg);
            }
        }

        return rtn;
    }

    private static List<String> getTagsWithoutAts(@Nonnull List<String> tags) {
        List<String> rtn = new ArrayList<>(tags.size());
        for(String tag : tags) {
            rtn.add(tag.replace("@", ""));
        }
        return rtn;
    }

    /**
     * I decided to add this because part of the reason we are using these feature files is because they are a readable
     * format. For this reason I think it is worth the little bit of time required to write this method to make sure
     * that the created feature files have pretty formatting.
     *
     * @return the content string for the scenario with the givens changed to ands if there was a background step.
     */
    private static String getContentWithNormalizedGivens(@Nonnull FeatureFile featureFile, @Nonnull Scenario scenario) {
        if (featureFile.getBackground() == null) return scenario.getContent();

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String line : scenario.getContentLines()) {
            if(!first) {
                sb.append("\n");
            } else {
                first = false;
            }
            sb.append(line.replaceFirst("Given", "And"));
        }

        return sb.toString();
    }

    private static HashSet<String> getTagsFromTagsLine(String tagsLine) {
        if (tagsLine == null) return new LinkedHashSet<>();
        // Use a linked hash set to preserve the order of the tags
        HashSet<String> rtn = new LinkedHashSet<>();

        if (tagsLine == null) return rtn;

        for (String tag : tagsLine.split(" ")) {
            rtn.add(tag.trim());
        }
        return rtn;
    }
}
