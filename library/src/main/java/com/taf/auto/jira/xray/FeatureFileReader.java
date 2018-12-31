package com.taf.auto.jira.xray;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class FeatureFileReader {

    private enum ScanMode {
        NONE,
        FEATURE,
        SCENARIOS
    }

    private final String projectKey;
    private ScanMode currScanMode;
    private FeatureFile currFeatureFile;
    private Scenario currScenario;
    private String currTopFeatureCommentLine;
    private String currTagLine;

    /**
     *
     * @param projectKey the name of the JIRA projectKey the featuer belongs to
     */
    public FeatureFileReader(String projectKey) {
        this.projectKey = projectKey;
        init();
    }

    private void init() {
        currScanMode = ScanMode.NONE;
        currFeatureFile = null;
        currScenario = null;
        currTagLine = null;
        currTopFeatureCommentLine = null;
    }

    public FeatureFile loadFromFile(String absoluteFilePath) throws IOException {
        return loadFromFile(new File(absoluteFilePath));
    }

    public FeatureFile loadFromFile(File file) throws IOException {
        try (Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()) {
                switch (currScanMode) {
                    case NONE:
                        processInModeNone(scanner);
                        break;
                    case FEATURE:
                        processInModeFeature(scanner);
                        break;
                    case SCENARIOS:
                        processInModeScenario(scanner);
                        break;
                    default:
                        break;
                }
            }
        }

        if (currFeatureFile != null) {
            currFeatureFile.setOriginalFileName(file.getAbsolutePath());
        }

        FeatureFile rtn = currFeatureFile;
        init();

        return rtn;
    }

    private void processInModeScenario(Scanner scanner) {
        boolean isExample = false;

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.isEmpty() || isCommentLine(line)) {
                continue;
            }

            if (isTagLine(line)) {
                currTagLine = line;
                continue;
            }
            
            if (isScenarioLine(line)) {
                currFeatureFile.appendScenario(currScenario);
                currScenario = new Scenario(line, currTagLine);
                currTagLine = null;
                continue;
            }

            if (line.contains("Examples:")) {
                isExample = true;
                currScenario.appendContentLine("");
                if (currTagLine != null) {
                    currScenario.appendContentLine(currTagLine);
                    currTagLine = null;
                }
                currScenario.appendContentLine(line);
                continue;
            }
            
            currScenario.appendContentLine(isExample || line.startsWith("|") ? "  " + line : line);
        }

        currFeatureFile.appendScenario(currScenario);
        currScanMode = ScanMode.NONE;
    }

    private void processInModeFeature(Scanner scanner) {
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (isTagLine(line)) {
                currTagLine = line;
            } else if (isScenarioLine(line)) {
                currScanMode = ScanMode.SCENARIOS;
                currScenario = new Scenario(line, currTagLine);
                currTagLine = null;

                break;
            }
        }
    }

    private void processInModeNone(Scanner scanner) {
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (isCommentLine(line)) {
                currTopFeatureCommentLine = line;
            } else if (isTagLine(line)) {
                currTagLine = line;
            } else if (isFeatureLine(line)) {
                currScanMode = ScanMode.FEATURE;
                currFeatureFile = new FeatureFile(line, currTopFeatureCommentLine, currTagLine, projectKey);
                currTagLine = null;
                currTopFeatureCommentLine = null;

                break;
            }
        }
    }

    private boolean isEmptyLine(String line) {
        return line.trim().isEmpty();
    }

    private boolean isCommentLine(String line) {
        return !isEmptyLine(line) && line.trim().charAt(0) == '#';
    }

    private boolean isTagLine(String line) {
        return !isEmptyLine(line) && line.trim().charAt(0) == '@';
    }

    private boolean isScenarioLine(String line) {
        return !isCommentLine(line)
                && !isTagLine(line)
                && line.contains(":")
                && (line.split(" ")[0].contains("Background") || line.split(" ")[0].contains("Scenario"));
    }

    private boolean isFeatureLine(String line) {
        return !isCommentLine(line)
                && !isTagLine(line)
                && line.contains(":")
                && line.split(" ")[0].contains("Feature");
    }

    public static void main(String[] args) throws Exception {
        String projectKey = "ANREIMAGED";
        FeatureFileReader reader = new FeatureFileReader(projectKey);
        FeatureFile featureFile = reader.loadFromFile("C:\\Users\\AD96317\\Documents\\Workspace\\scripts\\src\\test\\resources\\com\\anthem\\portal\\Benefits\\find_benefits_tab\\Benefit_Detail_Situation_Network_Costshare.feature");
        List<ScenarioForXray> scenarios = FeatureFileSplitter.splitFeatureFile(featureFile, projectKey);
    }
}
