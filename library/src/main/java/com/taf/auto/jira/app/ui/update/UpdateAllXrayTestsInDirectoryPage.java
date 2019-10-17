package com.taf.auto.jira.app.ui.update;

import com.taf.auto.jira.app.ui.ChooseFileOrDirectoryPage;
import com.taf.auto.jira.app.ui.ChooseModulePage.AboutModuleSupplier;

import java.io.File;

/**
 * UI for choosing the directory to process all contained 123.feature files.
 *
 */
public class UpdateAllXrayTestsInDirectoryPage extends ChooseFileOrDirectoryPage implements AboutModuleSupplier {
    public UpdateAllXrayTestsInDirectoryPage() {
        super("Feature", Mode.Directory, "Bulk Push To Xray");
    }

    @Override
    protected String peekMemoryPath() {
        return peekSettings().lastFeatureDir;
    }

    @Override
    protected void acceptFile(File file) {
        String absolutePath = file.getAbsolutePath();
        peekSettings().lastFeatureDir = absolutePath;
        saveSettings();
        changePage(new CollectFeatureFilesInDirectoryPage(file.toPath()));
    }

    @Override
    public String peekAboutMessage() {
        return "Scans the chosen directory for all files that begin with the project name and end in .feature.";
    }
}
