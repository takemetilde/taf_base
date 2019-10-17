package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.JFXThread;
import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jira.ITTeams;
import com.taf.auto.jira.app.ui.fetch.XrayFetchPage;
import com.taf.auto.jira.app.ui.migrate.ProcessNonXrayFeatureFile;
import com.taf.auto.jira.app.ui.update.UpdateAllXrayTestsInDirectoryPage;
import com.taf.auto.jira.app.ui.update.XrayBrowseUsersPage;
import com.taf.auto.jira.app.ui.update.XrayCreateUserPage;
import com.taf.auto.jira.app.ui.update.XrayUpdatePage;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import static com.taf.auto.jfx.JFXUI.Factory.label;
import static com.taf.auto.jfx.JFXUI.style;
import static javafx.scene.layout.BorderPane.setAlignment;

/**
 * Page to choose between available modules.
 *
 */
public class ChooseModulePage extends UIPage {

    private static boolean firstPass = true;

    public interface AboutModuleSupplier {
        String peekAboutMessage();
    }

    @Override
    protected String peekTitle() {
        return "Choose Module";
    }

    @Override
    protected Node buildContent() {
        UIPage[] pages = {
                new XrayFetchPage(),
                new XrayUpdatePage(),
                new ProcessNonXrayFeatureFile(),
                new XrayBrowseUsersPage(),
                new XrayCreateUserPage(),
                new UpdateAllXrayTestsInDirectoryPage(),
        };

        VBox box = style(new VBox(), "vbox-module-chooser");
        box.setPadding(new Insets(4));
        ObservableList<Node> children = box.getChildren();

        for(UIPage page : pages) {
            children.add(buildModuleButton(page));
        }

        Button buttonStartOver = new Button("Start Over");
        buttonStartOver.setOnAction(e -> {
            UISettings settings = peekSettings();
            settings.projectKey = UISettings.UNDEFINED;
            settings.poke(new ITTeams());
            changePage(new ConnectToJIRAPage());
        });
        BorderPane.setMargin(buttonStartOver, new Insets(4, 0, 0, 0));
        setAlignment(buttonStartOver, Pos.CENTER);

        Label label = new Label(formatProjectAndITTeam(peekSettings()));
        setAlignment(label, Pos.CENTER);

        ScrollPane sPane = new ScrollPane(box);
        sPane.setFitToWidth(true);

        BorderPane content = style(new BorderPane(sPane), "pane-padded");
        content.setTop(label);
        content.setBottom(buttonStartOver);
        return content;
    }

    static String formatProjectAndITTeam(UISettings settings) {
        return String.format("Project: %s (IT Team: %s)", settings.projectKey, settings.peek(ITTeams.class));
    }

    private Node buildModuleButton(UIPage page) {
        String about = page instanceof AboutModuleSupplier ?
            ((AboutModuleSupplier)page).peekAboutMessage() : "";

        Label labelAbout = new Label(about);
        labelAbout.setWrapText(true);
        labelAbout.setPrefHeight(40);
        labelAbout.setAlignment(Pos.TOP_LEFT);

        VBox area = new VBox(label(page.getTitle(), "label-choose-module-title"), labelAbout);

        Button button = new Button();
        button.setGraphic(area);
        button.setOnAction(event -> changePage(page));
        button.setMaxWidth(Double.MAX_VALUE);

        String module = System.getProperty("auto.XUA-module");
        if(firstPass && null != module && page.getClass().getName().equalsIgnoreCase(module)) {
            firstPass = false;
            JFXThread.jfxSafe(() -> button.fire(), true);
        }

        return button;
    }
}
