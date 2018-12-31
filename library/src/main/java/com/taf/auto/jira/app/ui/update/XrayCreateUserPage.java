package com.taf.auto.jira.app.ui.update;

import com.taf.auto.common.Handle;
import com.taf.auto.jfx.JFXUI;
import com.taf.auto.jfx.app.ui.UIFactory;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.app.ui.ChooseModulePage;
import com.taf.auto.jira.app.ui.ContinueOrCancelPage;
import com.taf.auto.jira.pojo.SelfKeyId;
import com.taf.auto.jira.request.CreateXrayPreConditionRequest;
import com.taf.auto.jira.xray.XrayUtil;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static com.taf.auto.jira.xray.XrayUtil.PreCondition.formatUserSummary;

/**
 * Created by AF04261 on 8/25/2017.
 */
public class XrayCreateUserPage extends ContinueOrCancelPage implements ChooseModulePage.AboutModuleSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(XrayCreateUserPage.class);

    private ComboBox<Object> comboEnv;
    private TextField textUsername;
    private TextField textNotes;
    private TextField textPattern;
    private TextArea textAttributes;

    @Override
    protected Node buildCenter(Button buttonCancel, Button buttonContinue) {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setHgap(4);
        pane.setVgap(4);

        AtomicInteger i = new AtomicInteger();
        BiConsumer<String, Node> add = (l, n) -> {
            pane.add(new Label(l), 0, i.get());
            pane.add(n, 1, i.getAndIncrement());
        };

        comboEnv = new ComboBox<>();
        comboEnv.getItems().addAll("DEV", "SIT");
        comboEnv.getSelectionModel().select(0);
        add.accept("Environment", comboEnv);

        textUsername = new TextField();
        add.accept("Username", textUsername);

        textNotes = new TextField();
        add.accept("Notes", textNotes);

        textPattern = new TextField("member logs in as \"%s\"");
        add.accept("Pattern", textPattern);

//        textAttributes = new TextArea();
//        add.accept("Attributes", textAttributes);

        return pane;
    }

    @Override
    protected void handleContinue(ActionEvent ae) {
        Handle<CreateXrayPreConditionRequest> request = new Handle<>(null);
        try {
            request.setValue(parseUserInput());
        } catch (Exception e) {
            LOG.trace("Failed to parse user input", e);
            showError(e.getMessage());
            return;
        }

        setBlocked(true);
        execute(() -> {
            try {
                SelfKeyId selfKeyId = JIRAUtil.createIssue(request.getValue(), peekSettings().peekCreds());
                changePage(new SuccessPage(selfKeyId.key));
            } catch (Exception e) {
                String msg = "Failed to create User";
                LOG.error(msg, e);
                showError(msg, e);
            } finally {
                setBlocked(false);
            }
        });
    }

    private CreateXrayPreConditionRequest parseUserInput() throws Exception {
        String username = textUsername.getText().trim();
        if(username.isEmpty()) {
            textUsername.requestFocus();
            throw new Exception("Please provide a Username.");
        }
        if(username.contains(" ")) {
            textUsername.requestFocus();
            throw new Exception("The Username may not contain a space.");
        }

        String notes = textNotes.getText().trim();
        if(notes.isEmpty()) {
            textNotes.requestFocus();
            throw new Exception("Please provide the Notes.");
        }

        String pattern = textPattern.getText().trim();
        if(pattern.isEmpty()) {
            textPattern.requestFocus();
            throw new Exception("Please provide the Pattern (e.g. \"member logs in as \"%s\"\".");
        }
        if(pattern.toLowerCase().startsWith("Given".toLowerCase())) {
            textPattern.requestFocus();
            throw new Exception("The Given keyword will be prepended automation. Please remove it.");
        }
        if(!pattern.contains("%s")) {
            textPattern.requestFocus();
            throw new Exception("The pattern is missing the variable placeholder: %s.");
        }

        String environment = comboEnv.getSelectionModel().getSelectedItem().toString();
        String summary = formatUserSummary(username, environment, notes);

        UISettings settings = peekSettings();
        CreateXrayPreConditionRequest request = new CreateXrayPreConditionRequest(settings.username, settings.projectKey, summary, XrayUtil.PreCondition.TEST_USER_LABEL_AS_ARRAY);
        request.fields.environment = environment;
        request.fields.conditions = "Given " + String.format(pattern, username);
        return request;
    }

    @Override
    public String peekAboutMessage() {
        return "Create a Pre-Condition modeled to be a test user";
    }

    @Override
    protected String peekTitle() {
        return "Create User";
    }

    private static class SuccessPage extends ContinueOrCancelPage {
        private final String key;

        public SuccessPage(String key) {
            this.key = key;
        }

        @Override
        protected Node buildCenter(Button buttonCancel, Button buttonContinue) {
            buttonCancel.setText("Create Another");
            buttonContinue.setText("Finished");

            FlowPane pane = new FlowPane(Orientation.VERTICAL);
            pane.setAlignment(Pos.CENTER);

            Button buttonView = new Button("View in JIRA");
            buttonView.setOnAction(e -> {
                try {
                    JIRAUtil.showInBrowser(key);
                } catch (Exception e1) {
                    JFXUI.Alerts.error("Failed to browse to: " + key + "\n" + e1.getMessage(), pane);
                }
            });

            pane.getChildren().addAll(
                    new Label("Created User: " + key), UIFactory.Pn.centered(buttonView));

            return pane;
        }

        @Override
        protected String peekTitle() {
            return "User Created";
        }

        @Override
        protected void handleContinue(ActionEvent ae) {
            changePage(new ChooseModulePage());
        }

        @Override
        protected void handleCancel(ActionEvent ae) {
            changePage(new XrayCreateUserPage());
        }
    }
}
