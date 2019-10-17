package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jira.xray.ScenarioForXray;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.List;

/**
 * UI to show while uploading the {@link ScenarioForXray} objects to JIRA.
 *
 */
public class UploadToJIRAPage extends UIPage {
    private final List<ScenarioForXray> scenarios;

    public UploadToJIRAPage(List<ScenarioForXray> scenarios) {
        this.scenarios = scenarios;
    }

    @Override
    protected String peekTitle() {
        return "Uploading Scenarios";
    }

    @Override
    protected Node buildContent() {
        execute(this::upload);
        return new Label("Please wait...");
    }

    private void upload() {

    }
}
