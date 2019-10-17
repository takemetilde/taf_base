package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jfx.app.ui.UISettings;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;

import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.JFXUI.Factory.button;
import static com.taf.auto.jfx.JFXUI.Factory.label;
import static com.taf.auto.jira.app.ui.ChooseModulePage.formatProjectAndITTeam;

/**
 * UI to confirm when there is an existing project in settings.
 *
 */
public class ConfirmProjectPage extends UIPage {
    @Override
    protected String peekTitle() {
        return "Confirm Project";
    }

    @Override
    protected Node buildContent() {
        UISettings settings = peekSettings();

        Button buttonSwitch = button("Reset", "button-switch-project", event -> {
            settings.projectKey = "";
            changePage(new ConnectToJIRAPage());
        });
        buttonSwitch.setTooltip(new Tooltip("Resets the Project and IT Team so you can choose again."));

        Label label = label(formatProjectAndITTeam(settings), "label-project");

        Button buttonContinue = button("Continue", "button-continue", event -> changePage(new ChooseModulePage()));

        FlowPane area = new FlowPane(buttonSwitch, label, buttonContinue);
        area.setVgap(4);
        area.setColumnHalignment(HPos.CENTER);
        area.setOrientation(Orientation.VERTICAL);
        area.setAlignment(Pos.CENTER);

        jfxSafe(() -> buttonContinue.requestFocus());

        return area;
    }
}
