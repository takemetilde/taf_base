package com.taf.auto.jira.app.ui.fetch;

import com.taf.auto.jfx.JFXUI;
import com.taf.auto.jfx.app.ui.ErrorPage;
import com.taf.auto.jfx.app.ui.InfoPage;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jfx.app.ui.UserCancelException;
import com.taf.auto.jira.IssueTypes;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.app.ui.ChooseFileOrDirectoryPage;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.app.ui.ContinueOrCancelPage;
import com.taf.auto.jira.app.ui.FailedKey;
import com.taf.auto.jira.pojo.AbstractIssue;
import com.taf.auto.jira.pojo.xray.*;
import com.taf.auto.jira.xray.ScenarioEmitter;
import com.taf.auto.jira.xray.XrayUtil;
import com.taf.auto.rest.UserPass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.taf.auto.IOUtil.mkdirs;
import static com.taf.auto.common.PrettyPrinter.prettyArray;
import static com.taf.auto.common.PrettyPrinter.prettyList;
import static com.taf.auto.io.JSONUtil.decode;
import static com.taf.auto.jfx.JFXCommon.setOnFirstSized;
import static com.taf.auto.jfx.app.ui.UIFactory.Lbl.labeled;
import static com.taf.auto.jira.app.ui.ChooseFileOrDirectoryPage.browseForFile;
import static com.taf.auto.jira.app.ui.FailedKey.formatFailedKeys;
import static java.lang.String.format;

/**
 * Page for fetching feature files from Xray Tests.
 *
 */
public class XrayFetchPage extends ContinueOrCancelPage implements ChooseModulePage.AboutModuleSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(XrayFetchPage.class);

    public static final String DEFAULT_FEATURE_FILE_DIR = "src/test/resources/com/anthem/auto";

    private TextField textDestination;
    private ComboBox<String> comboKey;

    public static class Store {
        @JsonIgnore
        @Deprecated
        public String projectKey = "";

        @JsonProperty
        public List<String> keyMemory;

        @JsonProperty
        public String destination = DEFAULT_FEATURE_FILE_DIR;

        public Store() {
            keyMemory = new LinkedList<>();
        }

        Store(List<String> keyMemory, String destination) {
            this.keyMemory = new LinkedList<>(keyMemory);
            this.destination = destination;
        }
    }

    @Override
    protected String peekTitle() {
        return "Fetch Feature Files from Xray Tests";
    }

    @Override
    public String peekAboutMessage() {
        return "Pulls Scenarios from Xray Tests as local files.";
    }

    @Override
    protected Node buildCenter(Button buttonCancel, Button buttonContinue) {
        UISettings settings = peekSettings();
        Store store = settings.peek(Store.class);

        textDestination = new TextField();
        BooleanProperty noDest = new SimpleBooleanProperty();
        textDestination.textProperty().addListener((obs, o, n) -> noDest.set(n.trim().isEmpty()));
        textDestination.setText(store.destination);
        textDestination.setOnAction(fireContinue());

        Button buttonBrowse = new Button("Browse...");
        buttonBrowse.setOnAction(evt ->
            browseForFile("Destination", ChooseFileOrDirectoryPage.Mode.Directory, Optional.empty(),
                    f -> textDestination.setText(f.toString()), peekStage()));
        BorderPane paneDestination = new BorderPane(textDestination);
        paneDestination.setRight(buttonBrowse);

        comboKey = new ComboBox<>();
        BooleanProperty noKey = new SimpleBooleanProperty();
        comboKey.getEditor().textProperty().addListener((obs, o, n) -> noKey.set(n.trim().isEmpty()));
        comboKey.setEditable(true);
        comboKey.setMaxWidth(Double.MAX_VALUE);
        comboKey.getSelectionModel().select(settings.projectKey + '-');
        comboKey.getItems().addAll(store.keyMemory);
        fireContinueOnEnter(comboKey, ke -> {
            if (ke.getCode() == KeyCode.DOWN) {
                comboKey.show();
            }
        });
        setOnFirstSized(comboKey, () -> {
            textDestination.requestFocus();
            comboKey.requestFocus();
            TextField editor = comboKey.getEditor();
            editor.positionCaret(comboKey.getValue().length());
        });

        GridPane gridPane = labeled(new String[]{ "JIRA Keys: ", "Destination: "}, new Node[]{comboKey, paneDestination});

        buttonContinue.disableProperty().bind(Bindings.or(noKey, noDest));

        return gridPane;
    }

    /**
     * Splits the input into separate keys.
     *
     * @param raw the string to split
     * @return split into keys
     */
    static String[] splitKeys(String raw) {
        String[] split = raw.split("[ ,;]");
        List<String> legit = new ArrayList<>(split.length);
        for(String candidate : split) {
            candidate = candidate.trim();
            if(!candidate.isEmpty()) {
                legit.add(candidate);
            }
        }
        String[] keys = legit.toArray(new String[legit.size()]);
        LOG.debug("Split: " + raw + "\nTo: " + prettyArray(keys));
        return keys;
    }

    @Override
    protected void handleContinue(ActionEvent ae) {
        String raw = comboKey.getEditor().getText();
        String[] keys = splitKeys(raw);
        Arrays.sort(keys);
        String processedKeys = prettyArray(keys);
        String dest = textDestination.getText();

        LOG.debug(format("Key: %s; Path: %s", processedKeys, dest));

        Path path = Paths.get(dest);
        if(!Files.isDirectory(path)) {
            if(JFXUI.Alerts.confirm(path.toAbsolutePath() + " does not exist. Would you like to create it?", peekDialogAnchor())) {
                try {
                    mkdirs(path);
                } catch (IOException e) {
                    JFXUI.Alerts.error("Failed to create directory: " + path.toAbsolutePath(), peekDialogAnchor());
                    return;
                }
            } else {
                return;
            }
        }

        boolean alreadyPresent = false;
        LinkedList<String> items = new LinkedList<>();
        for(String keyInMemory : comboKey.getItems()) {
            if(processedKeys.equals(keyInMemory)) {
                LOG.debug("Key is already present, moving to front of memory");
                alreadyPresent = true;
                items.addFirst(processedKeys);
            } else {
                items.add(keyInMemory);
            }
        }
        if(!alreadyPresent) {
            LOG.debug("Key NOT already present, adding at front of memory");
            items.addFirst(processedKeys);
        }
        if(items.size() > 13) {
            items = new LinkedList<>(items.subList(0, 5));
        }

        peekSettings().poke(new Store(items, dest));
        saveSettings();

        execute(() -> execute(splitKeys(processedKeys), path));
    }

    private static class FetchLog {
        List<String> fetchedTests = new ArrayList<>();
        List<FailedKey> failedKeys = new ArrayList<>();
    }

    private void execute(String[] keys, Path dest) {
        setBlocked(true);
        updateBlockedLabel("Verifying...");

        try {
            UserPass up = peekSettings().peekCreds();

            FetchLog log = new FetchLog();

            verify(keys, up).ifPresent(v -> {
                v.entrySet().forEach(e -> fetch(e.getKey(), e.getValue(), log, dest, up));
                if(log.failedKeys.isEmpty()) {
                    String msg = format("All Tests successfully downloaded to: %s\n\nManifest:\n%s", dest.toAbsolutePath(), prettyList(log.fetchedTests, "\n"));
                    changePage(new InfoPage(msg, new ChooseModulePage()));
                } else {
                    changePage(new ErrorPage(formatFailedKeys("The following Issues did not fetch correctly", log.failedKeys), new XrayFetchPage()));
                }
            });

        } catch (UserCancelException uce) {
            LOG.info("User canceled");
        } catch(Exception e) {
            String msg = "Unexpected error during fetch";
            LOG.error(msg, e);
            showError(msg, e);
        } finally {
            setBlocked(false);
        }
    }

    private Optional<Map<String, IssueTypes>> verify(String[] keys, UserPass up) {
        Map<String, IssueTypes> result = new LinkedHashMap<>();
        try {
            for(String key : keys) {
                updateBlockedLabel(format("Verifying %s...", key));
                IssueTypes type = JIRAUtil.fetchIssueType(key, up);
                switch (type) {
                    case Test: break;
                    case Test_Set:
                    case Test_Plan: if(!fetchContainedTests(type, key)) return Optional.empty(); break;
                    default:
                        JFXUI.Alerts.info(key + " is a " + type + " and is not a supported. Please verify this Issue and try again.", peekDialogAnchor());
                        return Optional.empty();
                }
                LOG.debug(format("Verified %s is a %s", key, type));
                result.put(key, type);
            }
            return Optional.of(result);
        } catch(Exception e) {
            String msg = "Failed to verify Keys";
            LOG.error(msg, e);
            showError(msg, e);
            return Optional.empty();
        }
    }

    private boolean fetchContainedTests(IssueTypes type, String key) {
        return showConfirmation(format("%s is a %s. All Xray Tests contained within will be fetched. Are you sure?", key, type));
    }

    private void fetch(String key, IssueTypes type, FetchLog log, Path dest, UserPass up) {
        LOG.info(format("Fetching %s %s...", type, key));
        if(type == IssueTypes.Test) {
            try {
                fetchTest(key, dest, log, up);
            } catch(UserCancelException uce) {
                LOG.debug("User canceled fetch");
                throw uce;
            } catch (Throwable t) {
                LOG.error("Failed to fetch single Test", t);
                log.failedKeys.add(new FailedKey(key, t));
            }
        } else if(type == IssueTypes.Test_Set || type == IssueTypes.Test_Plan) {
            fetchTests(type, key, dest, log, up);
        } else {
            throw new UnsupportedOperationException(format("% is not supported", type));
        }
    }

    private void fetchTest(String key, Path dest, FetchLog log, UserPass up) throws Exception {
        updateBlockedLabel(format("Fetching Test %s...", key));
        try {
            XrayTest test = JIRAUtil.fetchIssue(key, XrayTest.class, up);
            List<XrayPreCondition> users = XrayUtil.PreCondition.fetchUsers(test, up);

            Function<String, Path> pathMaker = k -> dest.resolve(k + ".feature");

            Map<Path, XrayTest> tests = new HashMap<>();

            if(users.isEmpty()) {
                tests.put(pathMaker.apply(key), test);
            } else {
                SelectUsersToFetchPage selectPage = new SelectUsersToFetchPage(this, test);
                changePage(selectPage);
                try {
                    List<XrayPreCondition> selectedUsers = selectPage.peekSelectedUsers();
                    LOG.debug("Selected Users: " + prettyList(selectedUsers.stream().map(u -> u.key).collect(Collectors.toList())));
                    for(XrayPreCondition user : selectedUsers) {
                        Pair<Path, XrayTest> pair = spinOutTestWithUser(test, user, pathMaker);
                        tests.put(pair.getKey(), pair.getValue());
                    }
                } finally {
                    setBlocked(true);
                }
            }

            tests.forEach(XrayFetchPage::emit);
            log.fetchedTests.add(key);
        } catch(UserCancelException uce) {
            throw uce;
        } catch (Throwable t) {
            throw new Exception("Failed to fetch Test: " + key, t);
        }
    }

    private static Pair<Path, XrayTest> spinOutTestWithUser(XrayTest test, XrayPreCondition user, Function<String, Path> pathMaker) throws IOException {
        int at = user.fields.summary.lastIndexOf('-');
        String name = user.fields.summary.substring(0, at).trim() ;
        String key = format("%s(%s)", test.key, name);
        return new Pair<>(pathMaker.apply(key), test.combineWith(user));
    }

    private static void emit(Path target, XrayTest test) {
        try {
            String emit = ScenarioEmitter.emit(test, Arrays.asList(test.fields.labels));

            Files.write(target, emit.getBytes());
            LOG.info(format("Wrote: %s to: %s", test.key, target.toAbsolutePath()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to emit: " + target, e);
        }
    }

    private void fetchTests(IssueTypes type, String key, Path dest, FetchLog log, UserPass up) {
        Class<? extends AbstractIssue<? extends XrayFields>> clazz;
        switch (type) {
            case Test_Set: clazz = XrayTestSet.class; break;
            case Test_Plan: clazz = XrayTestPlan.class; break;
            default: throw new UnsupportedOperationException("Not supported: " + type);
        }

        AbstractIssue<? extends XrayFields> issue;
        try {
            updateBlockedLabel(format("Fetching %s %s...", type, key));
            issue = decode(JIRAUtil.fetchIssue(key, up), clazz);
        } catch (Exception e) {
            LOG.error(format("Failed to fetch %s %s.", type, key), e);
            log.failedKeys.add(new FailedKey(key, e));
            return;
        }

        String[] keys = ((ContainsTests)issue.fields).peekTests();
        for(String containedKey : keys) {
            try {
                fetchTest(containedKey, dest, log, up);
            } catch (Exception e) {
                LOG.error("Failed to fetch bundled Test", e);
                log.failedKeys.add(new FailedKey(key, e));
            }
        }
    }
}
