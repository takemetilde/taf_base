package com.taf.auto.jira.app.ui.update;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.xray.ScenarioBundle;
import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.taf.auto.jfx.JFXUI.Factory.label;

public class ReviewCollectionErrorsPage extends UIPage {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewCollectionErrorsPage.class);

    private final List<CollectFeatureFileError> errors;
    private final List<ScenarioBundle> allScenarios;

    public ReviewCollectionErrorsPage(List<CollectFeatureFileError> errors, List<ScenarioBundle> allScenarios) {
        this.errors = errors;
        this.allScenarios = allScenarios;
    }

    @Override
    protected String peekTitle() {
        return "Review Collection Errors";
    }

    @Override
    protected Node buildContent() {
        VBox box = new VBox();
        box.setFillWidth(true);
        ObservableList<Node> children = box.getChildren();
        errors.forEach(error -> {
            Button buttonView = new Button("View");
            buttonView.setTooltip(new Tooltip("As the OS to open this feature file "));
            buttonView.setOnAction(event -> open(error.file));

            Button buttonIgnore = new Button("Ignore");
            buttonIgnore.setTooltip(new Tooltip("Ignore and skip processing this feature file"));

            HBox console = new HBox(buttonView, buttonIgnore);
            console.setSpacing(2);

            Label labelKey = label(error.key, "label-jira-key");
            BorderPane.setAlignment(labelKey, Pos.CENTER_LEFT);
            BorderPane top = new BorderPane(labelKey);
            top.setRight(console);

            Label label = new Label(error.msg);
            label.setWrapText(true);

            BorderPane pane = new BorderPane(label);
            pane.setTop(top);
            pane.setPadding(new Insets(2, 2, 0, 2));

            children.add(pane);

            buttonIgnore.setOnAction(event -> {
                ignore(error);
                FadeTransition ft = new FadeTransition(Duration.millis(350), pane);
                ft.setFromValue(.8);
                ft.setToValue(0);
                ft.setOnFinished(e2 -> children.remove(pane));

                ft.play();
            });
        });

        Button buttonCancel = new Button("Cancel");
        buttonCancel.setOnAction(e -> changePage(new ChooseModulePage()));

        Button buttonContinue = new Button("Continue");
        buttonContinue.setOnAction(e -> changePage(new CommitFeatureFilesInDirectoryPage(allScenarios)));
        buttonContinue.setDisable(true);

        children.addListener(((ListChangeListener.Change<? extends Node> c) -> {
            if(children.size() == 0)
                buttonContinue.setDisable(false);
        }));

        ScrollPane sPane = new ScrollPane(box);
        sPane.setFitToWidth(true);

        BorderPane pane = new BorderPane(sPane);
        pane.setBottom(buildConsole(buttonCancel, buttonContinue));
        return pane;
    }

    private void open(Path file) {
        try {
            Desktop.getDesktop().open(file.toFile());
        } catch (IOException e) {
            LOG.warn("Failed to open: " + file, e);
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.initOwner(peekStage());
            alert.showAndWait();
        }
    }

    private void ignore(CollectFeatureFileError error) {
        StatusCache.poke(error.key, IssueUpdateStatus.Ignore);
    }
}
