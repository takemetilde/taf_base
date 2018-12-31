package com.taf.auto.jira.app.ui.update;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.xray.ScenarioBundle;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

import static com.taf.auto.jfx.JFXUI.Factory.label;

public class ReviewUploadErrorsPage extends UIPage {
    private final List<UploadFeatureError> errors;

    public ReviewUploadErrorsPage(List<UploadFeatureError> errors) {
        this.errors = errors;
    }

    @Override
    protected String peekTitle() {
        return "Review Upload Errors";
    }

    @Override
    protected Node buildContent() {
        VBox box = new VBox();
        box.setFillWidth(true);
        ObservableList<Node> children = box.getChildren();
        errors.forEach(error -> {
            Label labelKey = label(error.item.peekKey(), "label-jira-key");
            BorderPane.setAlignment(labelKey, Pos.CENTER_LEFT);

            Label label = new Label(error.msg);
            label.setWrapText(true);

            BorderPane pane = new BorderPane(label);
            pane.setTop(labelKey);
            pane.setPadding(new Insets(2, 2, 0, 2));

            children.add(pane);
        });

        Button buttonRetry = new Button("Retry");
        buttonRetry.setOnAction(evt -> {
            List<ScenarioBundle> scenarios = errors.stream()
                    .map(e -> e.item)
                    .collect(Collectors.toList());
            changePage(new UploadFeatureFilesToXrayPage(scenarios));
        });

        Button buttonContinue = new Button("Finish");
        buttonContinue.setOnAction(e -> changePage(new ChooseModulePage()));

        ScrollPane sPane = new ScrollPane(box);
        sPane.setFitToWidth(true);

        BorderPane pane = new BorderPane(sPane);
        pane.setBottom(buildConsole(buttonRetry, buttonContinue));
        return pane;
    }
}
