package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.app.ui.UIPage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Page that provides a consistent Cancel and Continue buttons at the bottom.
 *
 * @author AF04261 mmorton
 */
public abstract class ContinueOrCancelPage extends ContinuePage {
    private Button buttonCancel;

    protected abstract Node buildCenter(Button buttonCancel, Button buttonContinue);

    @Override
    protected final Node buildCenter(Button buttonContinue) {
        buttonCancel = new Button("Cancel");
        buttonCancel.setOnAction(this::handleCancel);

        return buildCenter(buttonCancel, buttonContinue);
    }

    protected List<Node> buildBottom() {
        List<Node> bottom = new ArrayList<>();
        bottom.add(buttonCancel);
        bottom.addAll(super.buildBottom());
        return bottom;
    }

    protected UIPage defineCancelPage() {
        return new ChooseModulePage();
    }

    protected void handleCancel(ActionEvent ae) {
        changePage(defineCancelPage());
    }
}
