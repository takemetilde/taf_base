package com.taf.auto.jira.xray;

import org.junit.Test;

import static com.taf.auto.jira.xray.ScenarioEmitter.validate;

/**
 * Unit tests for {@link ScenarioEmitter}.
 *
 * @author AF04261 mmorton
 */
public class ScenarioEmitterTest {
    @Test
    public void validateTest() {
        String valid = "Feature: Valid example\n" +
                "Scenario Outline: Here we are\n" +
                "And I have navigated to the \"Claims\" page\n" +
                "When a member starts download claims\n" +
                "Then the download option \"<download option>\" is displayed\n" +
                "\n" +
                "Examples:\n" +
                "| download option |\n" +
                "| XLS             |\n" +
                "| CSV             |";
        validate(valid);
    }

    @Test(expected = MalformedFeatureException.class)
    public void validateMalformedTest() {
        String malformed = "Feature: Check if the selected cancer  topic opens in new tab when member select the any topics link \n" +
                "  @ANREIMAGED-31583 @xray-fetch\n" +
                "  Scenario Outline: Check if the selected cancer  topic opens in new tab when member select the any topics link \n" +
                "    Given member logs in as \"sit3SB210T90612\"\n" +
                "    And a member navigates to url \"/member/findcare/healthandwellnessprogramsv2\"\n" +
                "    When the member clicks on \"Learn more about cancer\" link\n" +
                "    And Cancer page should be opened\n" +
                "    And the member clicks on \"View full list of cancer topics\" link\n" +
                "    And the member clicks on \"<CancerMainLinks>\" button\n" +
                "    Then Select \"<CancerLinks>\" from the section and check if \"<Title>\" is opened in new tab\n" +
                "    \n" +
                "    Examples:\n" +
                "        |CancerMainLinks|       CancerLinks                 |           Title |  |Adrenocortical Cancer Treatment| Adrenocortical Cancer Treatment|    Adrenocortical  |\n" +
                "        |Breast Cancer|         Breast Cancer Treatment|Breast Cancer   |\n" +
                "        |Childhood Cancer|  Brain Stem Glioma Treatment|      Brain Stem Glioma ||Esophageal Cancer Treatment | Esophageal Cancer Treatment | Esophageal |\n";
        validate(malformed);
    }

    @Test(expected = MalformedFeatureException.class)
    public void validateEmptyTest() {
        String empty = "Feature: ";
        validate(empty);
    }
}
