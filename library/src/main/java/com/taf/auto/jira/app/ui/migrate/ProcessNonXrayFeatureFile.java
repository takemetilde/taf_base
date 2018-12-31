package com.taf.auto.jira.app.ui.migrate;

import com.taf.auto.jira.app.ui.ChooseFileOrDirectoryPage;
import com.taf.auto.jira.app.ui.ChooseModulePage.AboutModuleSupplier;

import java.io.File;

/**
 * UI for choosing a feature file that is not associated with an Xray test.
 *
 * @author AF04261 mmorton
 */
public class ProcessNonXrayFeatureFile extends ChooseFileOrDirectoryPage implements AboutModuleSupplier{
    public ProcessNonXrayFeatureFile() {
        super("Feature", ChooseFileOrDirectoryPage.Mode.File, "Create Xray Test(s)");
    }

    @Override
    protected String peekMemoryPath() {
        return peekSettings().originalFeatureFile;
    }

    protected void acceptFile(File file) {
        String absolutePath = file.getAbsolutePath();
        peekSettings().originalFeatureFile = absolutePath;
        logActivity("Processing: " + absolutePath);
        saveSettings();
        changePage(new ParseFeatureFilePage(file));
    }

    @Override
    public String peekAboutMessage() {
        return "Select a file containing one more Scenarios to be pushed to Xray as new Tests.";
    }
}
