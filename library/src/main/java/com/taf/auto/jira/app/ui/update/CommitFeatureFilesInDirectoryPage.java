package com.taf.auto.jira.app.ui.update;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.xray.ScenarioBundle;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.Consumer;

public class CommitFeatureFilesInDirectoryPage extends UIPage {
    private final List<ScenarioBundle> allScenarios;
    private BorderPane pane;

    public CommitFeatureFilesInDirectoryPage(List<ScenarioBundle> allScenarios) {
        this.allScenarios = allScenarios;
    }

    @Override
    protected String peekTitle() {
        return "Review Scenarios to Commit";
    }

    @Override
    protected Node buildContent() {
        Button buttonCancel = new Button("Cancel");
        buttonCancel.setOnAction(event -> changePage(new ChooseModulePage()));

        Button buttonCommit = new Button("Commit");
        buttonCommit.setOnAction(event -> changePage(new UploadFeatureFilesToXrayPage(allScenarios)));

        pane = new BorderPane(buildScenarios());
        pane.setLeft(buildTagsList());
        pane.setBottom(buildConsole(buttonCancel, buttonCommit));
        return pane;
    }

    private void deleteTags(List<String> tagsToDelete) {
        allScenarios.forEach(s -> {
            String[] tags = s.scenario.getTags();
            List<String> survivingTags = new ArrayList<>(tags.length);
            for(String tag : tags) {
                if(!tagsToDelete.contains(tag))
                    survivingTags.add(tag);
            }
            if(survivingTags.size() != tags.length) {
                s.scenario.setTags(survivingTags);
            }
        });

        pane.setCenter(buildScenarios());
    }

    private Node buildTagsList() {
        ListView<String> listTags = new ListView<>();
        listTags.getItems().addAll(collectTags());
        MultipleSelectionModel<String> selectionModel = listTags.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        ObservableList<String> selectedItems = selectionModel.getSelectedItems();

        Button buttonDelete = new Button("Delete");
        buttonDelete.setDisable(true);
        buttonDelete.setOnAction(e -> {
            deleteTags(selectedItems);
            listTags.getItems().setAll(collectTags());
        });

        selectedItems.addListener((ListChangeListener.Change<? extends String> c) -> {
            buttonDelete.setDisable(selectedItems.isEmpty());
        });

        BorderPane pane = new BorderPane(listTags);
        pane.setTop(new Label("All Tags"));
        pane.setBottom(buildConsole(buttonDelete));
        pane.setPrefWidth(180);
        return pane;
    }

    private Node buildScenarios() {
        VBox box = new VBox();
        box.setFillWidth(true);
        ObservableList<Node> children = box.getChildren();

        ScrollPane sPane = new ScrollPane(box);
        sPane.setFitToWidth(true);
        allScenarios.forEach(s -> {

            Label labelKey = new Label(s.peekKey());
            labelKey.setStyle("-fx-font-size: 14; -fx-font-weight: bold");
            BorderPane.setAlignment(labelKey, Pos.CENTER_LEFT);
            BorderPane top = new BorderPane(labelKey);

            FlowPane tagPane = new FlowPane();
            tagPane.setHgap(2);
            tagPane.setVgap(2);
            ObservableList<Node> children1 = tagPane.getChildren();
            s.scenario.peekSortedTags().forEach(tag -> children1.add(buildTagLabel(tag, tagPane)));

            BorderPane pane = new BorderPane(tagPane);
            pane.setTop(top);

            pane.setPadding(new Insets(2, 2, 0, 2));

            children.add(pane);
        });

        return sPane;
    }

    private Node buildTagLabel(String tag, FlowPane parent) {
//        Button buttonRemove = new Button();
//        buttonRemove.setStyle("-fx-background-color: transparent; -fx-border: null; -fx-padding: 0; -fx-graphic: url('img/close.png')");
//        BorderPane.setAlignment(buttonRemove, Pos.TOP_CENTER);
//        BorderPane.setMargin(buttonRemove, new Insets(0, 0, 0, 2));
//
//        BorderPane right = new BorderPane();
//        right.setTop(buttonRemove);
//        BorderPane.setAlignment(right, Pos.TOP_CENTER);

        Label tagLabel = new Label(tag);
        BorderPane pane = new BorderPane(tagLabel);
//        pane.setRight(right);
        pane.setStyle("-fx-border-color: gray; -fx-border-radius: 2; -fx-padding: 2");

//        buttonRemove.setOnAction(e -> {
//            parent.getChildren().remove(pane);
//        });

        return pane;
    }

    private List<String> collectTags() {
        Set<String> tags = new HashSet<>();
        Consumer<ScenarioBundle> tagCollector = s -> {
            for(String tag : s.scenario.getTags())
                tags.add(tag);
        };

        allScenarios.forEach(tagCollector);

        List<String> sortedTags = new ArrayList<>(tags);
        Collections.sort(sortedTags);
        return sortedTags;
    }
}
