package com.taf.auto.jira.app.ui.update;

import com.taf.auto.StringUtil;
import com.taf.auto.jfx.JFXThread;
import com.taf.auto.jfx.JFXUI;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.app.ui.ContinuePage;
import com.taf.auto.jira.pojo.xray.XrayPreCondition;
import com.taf.auto.jira.xray.XrayUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.JFXUI.style;

/**
 * Created by AF04261 on 8/24/2017.
 */
public class XrayBrowseUsersPage extends ContinuePage implements ChooseModulePage.AboutModuleSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(XrayBrowseUsersPage.class);

    private TableView<XrayPreCondition> table;

    private List<XrayPreCondition> allPreConditions;

    @Override
    protected Node buildCenter(Button buttonContinue) {
        buttonContinue.setText("Done");

        BorderPane pane = new BorderPane();
        execute(() -> {
            try {
                allPreConditions = XrayUtil.PreCondition.fetchAll(peekSettings().projectKey, peekSettings().peekCreds());
                setBlocked(false);
                JFXThread.jfxSafe(() -> pane.setCenter(buildResults()));
            } catch (IOException e) {
                showError("Failed to fetch Users", e);
                changePage(new ChooseModulePage());
            }
        });
        setBlocked(true);
        updateBlockedLabel("Fetching Pre-Condition Users...");
        return pane;
    }

    private Node buildResults() {
        table = new TableView<>();
        table.getItems().addAll(allPreConditions);

        TableColumn<XrayPreCondition, String> colKey = new TableColumn<>("Key");
        colKey.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().key));

        TableColumn<XrayPreCondition, String> colEnv = new TableColumn<>("ENV");
        colEnv.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().fields.environment));

        TableColumn<XrayPreCondition, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(row -> new ReadOnlyStringWrapper(extractUsername(row.getValue())));

        TableColumn<XrayPreCondition, String> colNotes = new TableColumn<>("Notes");
        colNotes.setCellValueFactory(row -> new ReadOnlyStringWrapper(extractNotes(row.getValue())));

        table.getColumns().addAll(colKey, colEnv, colUser, colNotes);

        SplitPane splitPane = new SplitPane(table, buildDetail());
        splitPane.setOrientation(Orientation.VERTICAL);

        BorderPane pane = style(new BorderPane(splitPane), "pane-padded");
        pane.setBottom(buildFinder());
        return pane;
    }

    private static Predicate<XrayPreCondition> isFinderMatch(String find) {
        return p -> {
            if(p.fields.summary.toLowerCase().contains(find)) {
                return true;
            }
            if(null != p.fields.description && p.fields.description.toLowerCase().contains(find)) {
                return true;
            }
            return false;
        };
    }

    private static List<XrayPreCondition> applyFilter(List<XrayPreCondition> all, String filter) {
        if(null == filter || filter.isEmpty()) {
            return all;
        }
        return all.stream().filter(isFinderMatch(filter)).collect(Collectors.toList());
    }

    private Node buildFinder() {
        TextField textFinder = new TextField();
        textFinder.textProperty().addListener((o, old, neo) -> {
            String filter = neo.trim().toLowerCase();
            JFXThread.jfxSafe(() -> {

                ObservableList<XrayPreCondition> items = table.getItems();
                items.setAll(applyFilter(allPreConditions, filter));
                if(items.isEmpty()) {
                    table.getSelectionModel().clearSelection();
                } else {
                    table.getSelectionModel().select(0);
                }
            });
        });
        fireContinueOnEnter(textFinder);
        BorderPane pane = new BorderPane(textFinder);
        Label labelFinder = new Label("Filter: ");
        BorderPane.setAlignment(labelFinder, Pos.CENTER);
        pane.setLeft(labelFinder);

        jfxSafe(() -> textFinder.requestFocus(), true);

        return pane;
    }

    private Optional<XrayPreCondition> peekSelected() {
        XrayPreCondition selectedItem = table.getSelectionModel().getSelectedItem();
        return Optional.ofNullable(selectedItem);
    }

    private Node buildDetail() {
        ListView<String> listAttributes = new ListView<>();
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> items = listAttributes.getItems();
            if(null == newValue) {
                items.clear();
            } else {
                if(null != newValue.fields.description) {
                    items.setAll(StringUtil.splitNewlines(newValue.fields.description));
                } else {
                    items.clear();
                }
            }
        });

        Button buttonView = new Button("View in JIRA");
        buttonView.setOnAction(e -> peekSelected().ifPresent(precon -> {
            try {
                JIRAUtil.showInBrowser(precon.key);
            } catch (Exception e1) {
                JFXUI.Alerts.error("Failed to browse to: " + precon.key + "\n" + e1.getMessage(), table);
            }
        }));

        FlowPane console = new FlowPane(buttonView);

        BorderPane pane = new BorderPane(listAttributes);
        pane.setTop(console);
        return pane;
    }

    private static String extractUsername(XrayPreCondition precon) {
        return extractFromSummary(precon.fields.summary, 1, "Username");
    }

    private static String extractNotes(XrayPreCondition precon) {
        return extractFromSummary(precon.fields.summary, 2, "Notes");
    }

    private static String extractFromSummary(String summary, int i, String type) {
        try {
            String[] split = summary.split(Pattern.quote("-"));
            return split[i].trim();
        } catch(Exception e) {
            LOG.error("Failed to extract {} from: {}", type, summary);
            return "?";
        }
    }

    @Override
    protected void handleContinue(ActionEvent ae) {
        changePage(new ChooseModulePage());
    }

    @Override
    protected String peekTitle() {
        return "Browse Users";
    }

    @Override
    public String peekAboutMessage() {
        return "Browse the Pre-Condition Users";
    }
}
