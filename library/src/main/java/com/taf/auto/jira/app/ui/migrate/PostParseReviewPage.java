package com.taf.auto.jira.app.ui.migrate;

import com.taf.auto.common.PrettyPrinter;
import com.taf.auto.jfx.JFXUI;
import com.taf.auto.jfx.app.ui.ErrorPage;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jira.ITTeams;
import com.taf.auto.jira.IssueTypes;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.app.ui.ContinueOrCancelPage;
import com.taf.auto.jira.app.ui.PleaseWaitPage;
import com.taf.auto.jira.pojo.SelfKeyId;
import com.taf.auto.jira.xray.ScenarioForXray;
import com.taf.auto.jira.xray.SingleScenarioWriter;
import com.taf.auto.jira.xray.XrayTestStatuses;
import com.taf.auto.jira.xray.XrayUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.taf.auto.common.PrettyPrinter.prettyException;
import static com.taf.auto.jfx.JFXCommon.setOnFirstSized;
import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.JFXUI.Factory.label;
import static com.taf.auto.jfx.JFXUI.style;
import static java.lang.String.format;

/**
 * UI for reviewing the parse results.
 *
 */
public class PostParseReviewPage extends ContinueOrCancelPage {
    private static final Logger LOG = LoggerFactory.getLogger(PostParseReviewPage.class);

    private final List<ScenarioForXray> scenarios;
    private JIRAIssueChooserPane storyChooser;

    private SimpleIntegerProperty commitsRemaining;
    private TabPane tabPane;

    public PostParseReviewPage(List<ScenarioForXray> scenarios) {
        this.scenarios = scenarios;
        commitsRemaining = new SimpleIntegerProperty(scenarios.size());
    }

    @Override
    protected String peekTitle() {
        return "Commit Scenarios to JIRA";
    }

    @Override
    protected Node buildCenter(Button buttonCancel, Button buttonContinue) {
        buttonContinue.setDisable(true);

        commitsRemaining.addListener((obs, o, n) -> {
            if(n.intValue() == 0) {
                buttonContinue.setDisable(false);
                buttonCancel.setDisable(true);
            }
        });

        tabPane = new TabPane();
        ObservableList<Tab> tabs = tabPane.getTabs();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        int index = 1;
        for(ScenarioForXray scenario : scenarios) {
            tabs.add(new ScenarioTab(scenario, index++).tab);
        }

        storyChooser = new JIRAIssueChooserPane("Related User Story:", peekSettings().projectKey, this, IssueTypes.Story, IssueTypes.Defect);

        Label labelTeam = new Label("IT Team: " + peekSettings().peek(ITTeams.class));
        BorderPane.setAlignment(labelTeam, Pos.CENTER);

        BorderPane paneTop = style(new BorderPane(), "pane-padded");
        paneTop.setLeft(storyChooser);
        paneTop.setRight(labelTeam);

        BorderPane area = new BorderPane(tabPane);
        area.setTop(paneTop);

        setOnFirstSized(area, () -> storyChooser.fire());

        return area;
    }

    private class ScenarioTab extends BorderPane {
        private final ScenarioForXray scenario;

        private Tab tab;

        ScenarioTab(ScenarioForXray scenario, int index) {
            this.scenario = scenario;

            Label labelFeatureName = monoLabel(scenario.getFeatureName());
            Label labelScenarioName = monoLabel(scenario.getScenarioName());
            Label labelTags = monoLabel(PrettyPrinter.prettyArray(scenario.getTags()));
            labelTags.setTooltip(new Tooltip(labelTags.getText()));

            GridPane grid = new GridPane();
            grid.setPadding(new Insets(4));
            grid.add(new Label("Feature Name"), 0, 0);
            grid.add(labelFeatureName, 1, 0);
            Label label = new Label("Scenario Name");
            label.setMinWidth(90);
            grid.add(label, 0, 1);
            grid.add(labelScenarioName, 1, 1);
            grid.add(new Label("Tags"), 0, 2);
            grid.add(labelTags, 1, 2);
            grid.add(new Label("Type"), 0, 3);
            grid.add(monoLabel(scenario.getType()), 1, 3);

            TextArea textScenario = style(new TextArea(), "text-monospace");
            textScenario.setEditable(false);
            textScenario.setText(scenario.getContent());

            Button buttonCommit = new Button("Commit");
            buttonCommit.setOnAction(this::commit);

            BorderPane.setAlignment(buttonCommit, Pos.CENTER);

            BorderPane top = new BorderPane();
            top.setTop(buttonCommit);
            top.setBottom(grid);

            setTop(top);
            setCenter(textScenario);
            style(this, "pane-padded");

            tab = new Tab("Scenario " + Integer.toString(index), this);
        }

        private void commit(ActionEvent event) {
            if(!storyChooser.peekIssueKey().isPresent()) {
                JFXUI.Alerts.error("Please choose the related User Story first and try again.", peekDialogAnchor());
                return;
            }

            changePage(new PleaseWaitPage(this::commitLogic));
        }

        private Optional<XrayTestStatuses> resolveStatus(ScenarioForXray scenario) {
            return scenario.hasTag("wip") ? Optional.empty() : Optional.of(XrayTestStatuses.TestReady);
        }

        /**
         * <ol>
         * <li>Creates the Xray test
         * <li>Writes the feature file
         * <li>Updates the JIRA issue with a self reference label
         * </ol>
         */
        private void commitLogic() {
            UISettings settings = peekSettings();
            try {
                SelfKeyId response;
                try {
                    String assignee = settings.username.toUpperCase();
                    String storyToTest = storyChooser.peekIssueKey().get();
                    ScenarioForXray finalScenario = scenario.cloneWithJIRAIDs(storyToTest);

                    // if there is a redundant tag for the tested story, remove it
                    finalScenario = finalScenario.cloneWithTagsRemoved(Collections.singletonList(storyToTest));

                    response = XrayUtil.createXrayTest(assignee, settings.projectKey, finalScenario,
                            resolveStatus(finalScenario), Optional.of(settings.peek(ITTeams.class)), settings.username, settings.password);
                    String msg = "Test created: " + response.key;
                    LOG.info(msg);
                    logActivity(msg);
                } catch (Exception e) {
                    throw new Exception("Failed to create Xray test on JIRA", e);
                }

                logActivity("Successfully processed: " + scenario.getScenarioName());
                jfxSafe(() -> updateWithSuccess(response.key));

                // return from please wait page
                changePage(PostParseReviewPage.this);
            } catch (Throwable t) {
                String msg = "Failure during commit";
                LOG.error(msg, t);
                logActivity(msg + ": " + t.getMessage());
                // unblock the tab if there is a failure
                jfxSafe(() -> setDisable(false));
                // the error page will switch back to this page
                changePage(new ErrorPage(msg + ":\n" + prettyException(t), PostParseReviewPage.this));
            }
        }

        private void writeScenarioLocally(SelfKeyId response) {
            String filename = response.key + ".feature";

            String scenarioContent;
            try {
                scenarioContent = SingleScenarioWriter.formatScenarioContent(response.key, scenario);
            } catch(Exception e) {
                String msg = "Failed to format scenario content. Please create the file: " + filename + " by hand.";
                LOG.error(msg, e);
                return;
            }

            try {
                File parent = new File(peekSettings().originalFeatureFile).getParentFile();
                File file = new File(parent, filename);
                SingleScenarioWriter.writeSingleScenarioToFeatureFile(file, scenarioContent);
                String activity = "Wrote feature file: " + file;
                LOG.info(activity);
                logActivity(activity);
            } catch (Exception e) {
                String msg = "Failed to create: " + filename;
                LOG.error(msg, e);
                JFXUI.Alerts.error(msg + "\n\nPlease edit JIRA by hand to add this label: " + response.key +
                        "\n\nAnd create a file with this content:\n" + scenarioContent, peekDialogAnchor() );
            }
        }

        private void updateWithSuccess(String key) {
            Button button = new Button("View in Browser");
            button.setOnAction(evt -> {
                try {
                    JIRAUtil.showInBrowser(key);
                } catch (Exception e) {
                    JFXUI.Alerts.error("Failed to browse to: " + key + "\n" + e.getMessage(), ScenarioTab.this);
                }
            });

            VBox area = new VBox(new Label("Created Xray Test: " + key), button);
            area.setAlignment(Pos.CENTER);
            BorderPane.setAlignment(area, Pos.CENTER);
            setCenter(area);
            getTop().setDisable(true);

            commitsRemaining.set(commitsRemaining.get() - 1);

            tab.setText(key);

            selectNextTab();
        }

        private void selectNextTab() {
            ObservableList<Tab> tabs = tabPane.getTabs();
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            for(int i = tabs.indexOf(tab) + 1, len = tabs.size(); i < len; i++) {
                Tab nextTab = tabs.get(i);
                if(nextTab.getText().startsWith("Scenario")) {
                    selectionModel.select(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void handleCancel(ActionEvent ae) {
        if (JFXUI.Alerts.confirm("Are you sure you want to cancel?", peekDialogAnchor())) {
            changePage(new ProcessNonXrayFeatureFile());
        }
    }

    @Override
    protected void handleContinue(ActionEvent ae) {
        File file = new File(peekSettings().originalFeatureFile);
        if(JFXUI.Alerts.confirm(format("Delete %s?", file.getAbsolutePath()), peekDialogAnchor())) {
            boolean deleted = file.delete();
            if(!deleted) {
                JFXUI.Alerts.error("Unable to delete: " + file.getAbsolutePath(), peekDialogAnchor());
            }
        }
        changePage(new ChooseModulePage());
    }

    private static Label monoLabel(String text) {
        return label(text, "text-monospace");
    }
}
