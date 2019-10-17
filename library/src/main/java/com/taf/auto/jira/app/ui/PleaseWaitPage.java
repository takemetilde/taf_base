package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.app.ui.UIPage;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * UI to show while waiting for an execution to complete.
 *
 */
public class PleaseWaitPage extends UIPage {
    private final Runnable logic;

    /**
     *
     * @param logic the logic to run - it must transition to a different page as needed
     */
    public PleaseWaitPage(Runnable logic) {
        this.logic = logic;
    }

    @Override
    protected String peekTitle() {
        return "Please Wait";
    }

    @Override
    protected Node buildContent() {
        execute(logic);
        return new Label("Please wait...");
    }
}