package com.taf.auto.jira.app.ui.update;

import com.taf.auto.jfx.JFXUI;
import com.taf.auto.jfx.app.ui.ErrorPage;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jira.IssueHandle;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.app.ui.ContinueOrCancelPage;
import com.taf.auto.jira.app.ui.FailedKey;
import com.taf.auto.jira.pojo.xray.XrayTest;
import com.taf.auto.jira.xray.ScenarioForXray;
import com.taf.auto.jira.xray.XrayUtil;
import com.taf.auto.rest.UserPass;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.taf.auto.IOUtil.NL;
import static com.taf.auto.StringUtil.splitNewlines;
import static com.taf.auto.common.PrettyPrinter.prettyArray;
import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.JFXUI.style;
import static com.taf.auto.jira.app.ui.FailedKey.formatFailedKeys;
import static com.taf.auto.jira.app.ui.fetch.XrayFetchPage.DEFAULT_FEATURE_FILE_DIR;
import static java.lang.String.format;

/**
 * Page for updating existing Xray Tests.
 *
 * @author AF04261
 */
public class XrayUpdatePage extends ContinueOrCancelPage implements ChooseModulePage.AboutModuleSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(XrayUpdatePage.class);

    private ListView<File> listFiles;
    private List<FailedKey> failedUpdates;

    public static class Store {
        @JsonProperty
        public String source = DEFAULT_FEATURE_FILE_DIR;

        public Store() {
        }

        Store(String projectKey, String source) {
            this.source = source;
        }
    }

    @Override
    protected String peekTitle() {
        return "Update Xray Tests From Feature Files";
    }

    @Override
    public String peekAboutMessage() {
        return "Updates already existing Xray Tests from local .feature files";
    }


    @Override
    protected Node buildCenter(Button buttonCancel, Button buttonContinue) {
        Button buttonBrowse = new Button("Browse...");
        buttonBrowse.setOnAction(this::browseForFile);
        BorderPane.setAlignment(buttonBrowse, Pos.CENTER);

        listFiles = new ListView<>();
        listFiles.setCellFactory( c -> new ListCell<File>() {
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText(null == item ? "" : item.getName() + " - " + item.getAbsolutePath() );
            }
        });
        listFiles.setOnKeyReleased(this::typeInFileList);

        ObservableList<File> items = listFiles.getItems();
        items.addListener((ListChangeListener.Change<? extends File> c) -> {
            buttonContinue.setDisable(items.isEmpty());
        });
        buttonContinue.setDisable(true);

        BorderPane.setMargin(buttonBrowse, new Insets(0, 0, 4, 0));
        BorderPane pane = style(new BorderPane(listFiles), "pane-padded");
        pane.setTop(buttonBrowse);
        return pane;
    }

    private void typeInFileList(KeyEvent ae) {
        switch(ae.getCode()) {
            case DELETE: deleteSelected(); break;
        }
    }

    private void deleteSelected() {
        ObservableList<File> selectedItems = listFiles.getSelectionModel().getSelectedItems();
        listFiles.getItems().removeAll(selectedItems);
    }

    private void browseForFile(ActionEvent event) {
        UISettings settings = peekSettings();
        Store store = settings.peek(Store.class);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Browse for Feature Files");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Cucumber Feature File", "*.feature"));
        File initial = store.source.isEmpty() ? new File("").getAbsoluteFile() : new File(store.source);
        if(!initial.exists()) {
            initial = new File("").getAbsoluteFile();
        }
        fileChooser.setInitialDirectory(initial);
        List<File> files = fileChooser.showOpenMultipleDialog(peekStage());

        if(null == files || files.isEmpty()) return;

        File parentFile = files.get(0).getParentFile();
        store.source = parentFile.toString();
        LOG.debug("Capturing source path: " + store.source);
        settings.poke(store);
        saveSettings();

        ObservableList<File> items = listFiles.getItems();
        files.forEach(f -> {
            if(!f.getName().startsWith(settings.projectKey)) {
                LOG.warn(format("Ignoring feature file: %s that doesn't start with: %s", f.getAbsolutePath(), settings.projectKey));
                JFXUI.Alerts.error("Ignoring non-Xray feature file: " + f, peekDialogAnchor());
            } else {
                /** don't add the file if it's already present */
                if (!items.contains(f)) items.add(f);
            }
        });
    }

    @Override
    protected void handleContinue(ActionEvent ae) {
        execute(() -> {
            setBlocked(true);
            try {
                List<Pair<File, ScenarioForXray>> scenarios;
                try {
                    scenarios = loadScenarios(listFiles.getItems());
                } catch(Exception e) {
                    JFXUI.Alerts.error("Invalid Feature File detected: " + e.getMessage(), peekDialogAnchor());
                    return;
                }
                UserPass creds = peekSettings().peekCreds();
                failedUpdates = new ArrayList<>();
                scenarios.forEach(p -> upload(p, creds));
            } catch (Exception e) {
                LOG.error("Failed to push", e);
                JFXUI.Alerts.error("Failed to push: " + e.getMessage(), peekDialogAnchor());
            } finally {
                setBlocked(false);
            }
            jfxSafe(() -> {
                if(listFiles.getItems().isEmpty()) {
                    JFXUI.Alerts.info("All Xray Tests successfully updated.", peekDialogAnchor());
                } else {
                    changePage(new ErrorPage(formatFailedKeys("The following Xray Tests could not be updated", failedUpdates), XrayUpdatePage.this));
                }
            });
        });
    }

    private List<Pair<File, ScenarioForXray>> loadScenarios(List<File> files) throws Exception {
        String projectKey = peekSettings().projectKey;
        return files.stream()
                .map(f -> {
                    List<ScenarioForXray> scenarios;
                    try {
                        scenarios = ScenarioForXray.parseScenarios(f, projectKey);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse: " + f, e);
                    }
                    if(scenarios.size() != 1) {
                        throw new RuntimeException(f.getAbsolutePath() + " must contain exactly 1 Scenario");
                    }
                    return new Pair<>(f, scenarios.get(0));
                })
                .collect(Collectors.toList());
    }

    private static String extractKey(File file) {
        String name = file.getName();
        try {
            String key = name.substring(0, name.indexOf(".feature"));
            int at = key.indexOf('(');
            if(at >= 0) {
                key = key.substring(0, at);
            }
            return key;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract key from: " + name, e);
        }
    }

    private void upload(Pair<File, ScenarioForXray> pair, UserPass creds) {
        File file = pair.getKey();
        LOG.info("Uploading: " + file.getAbsolutePath());
        String key = extractKey(file);
        try {
            updateBlockedLabel(format("Updating: %s...", key));
            IssueHandle handle = new IssueHandle(key);

            XrayTest currentTest;
            try {
                currentTest = XrayUtil.fetchTest(key, creds);
            } catch (Exception e) {
                throw new Exception("Failed to fetch Test in order to determine if it is Closed", e);
            }
            if(JIRAUtil.isIssueClosed(currentTest)) {
                throw new Exception("Unable to update because this Test is CLOSED.");
            }

            ScenarioForXray scenario = pair.getValue();
            scenario = stripUser(scenario);
            XrayUtil.updateScenario(scenario, handle, creds);
            JIRAUtil.Label.add(handle, Arrays.asList(scenario.getTags()), creds);
            jfxSafe(() -> listFiles.getItems().remove(file));
        } catch (Throwable t) {
            LOG.error("Failed to push: " + file.getAbsolutePath(), t);
            failedUpdates.add(new FailedKey(key, t));
        }
    }

    private static final String[] PRECONDITIONS = {
            "Given member logs in as"
    };

    private static ScenarioForXray stripUser(ScenarioForXray scenario) {
        String content = scenario.getContent();
        String[] lines = splitNewlines(content);
        String firstLine = lines[0].trim();
        boolean strip = false;
        for(String precondition : PRECONDITIONS) {
            if(firstLine.startsWith(precondition)) {
                strip = true;
                break;
            }
        }

        if(strip) {
            content = prettyArray(lines, 1, lines.length, NL);
            LOG.info(format("Scenario was stripped of: %s", firstLine));
            return scenario.cloneWithUpdatedScenario(content, scenario.getType());
        } else {
            return scenario;
        }
    }
}
