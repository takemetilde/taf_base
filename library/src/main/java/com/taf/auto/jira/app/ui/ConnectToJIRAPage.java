package com.taf.auto.jira.app.ui;

import com.taf.auto.common.PrettyPrinter;
import com.taf.auto.jfx.app.ui.ErrorPage;
import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jira.ITTeams;
import com.taf.auto.jira.JIRAUtil;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.taf.auto.jfx.JFXThread.jfxSafe;

/**
 * UI for logging in to JIRA.
 *
 * @author AF04261 mmorton
 */
public class ConnectToJIRAPage extends UIPage {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectToJIRAPage.class);

    private Pane area;
    private TextField textUser;
    private PasswordField textPassword;

    private static boolean firstPass = true;

    @Override
    protected String peekTitle() {
        return "Connect to JIRA";
    }

    @Override
    protected Node buildContent() {
        UISettings settings = peekSettings();
        textUser = new TextField(settings.username);
        textPassword = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(4);
        grid.add(new Label("Username"), 0, 0);
        grid.add(textUser, 1, 0);
        grid.add(new Label("Password"), 0, 1);
        grid.add(textPassword, 1, 1);

        Button buttonConnect = new Button("Connect");
        BorderPane.setAlignment(buttonConnect, Pos.CENTER);
        BorderPane.setMargin(buttonConnect, new Insets(4, 0, 0, 0));
        buttonConnect.setOnAction(this::connect);

        textUser.setOnAction(event -> buttonConnect.fire());
        textPassword.setOnAction(event -> buttonConnect.fire());

        BorderPane.setAlignment(grid, Pos.CENTER);

        BorderPane center = new BorderPane(grid);
        center.setBottom(buttonConnect);

        FlowPane flowPane = new FlowPane(center);
        flowPane.setAlignment(Pos.CENTER);

        jfxSafe(() -> {
            boolean hasUsername = !textUser.getText().isEmpty();
            if(hasUsername)
                textPassword.requestFocus();

            String pass = System.getProperty(JIRAUtil.Constants.JIRA_PASS);
            if(null != pass) {
                textPassword.setText(pass);
                if(hasUsername && firstPass) {
                    firstPass = false;
                    buttonConnect.fire();
                }
            }

        }, true);

        area = flowPane;
        return area;
    }

    private void connect(ActionEvent event) {
        String username = textUser.getText().trim();
        if(username.isEmpty()) {
            textUser.requestFocus();
            return;
        }
        String password = textPassword.getText().trim();
        if(password.isEmpty()) {
            textPassword.requestFocus();
            return;
        }

        area.setDisable(true);

        logActivity("Connecting with user: " + username);
        setBlocked(true);
        execute(() -> {
            try {
                UISettings settings = peekSettings();
                settings.username = username;
                settings.password = password;
                saveSettings();

                if(settings.projectKey.isEmpty() || settings.peek(ITTeams.class).peekUndefined()) {
                    updateBlockedLabel("Fetching list of Projects...");
                    List<String> projectKeys = new ArrayList<>(JIRAUtil.fetchProjects(username, password));
                    Collections.sort(projectKeys);
                    LOG.debug("Fetched projectKeys: " + PrettyPrinter.prettyCollection(projectKeys));
                    changePage(new ChooseProjectPage(projectKeys));
                } else {
                    updateBlockedLabel("Authenticating...");
                    JIRAUtil.authenticate(username, password);
                    UIPage nextPage = null == System.getProperty(JIRAUtil.Constants.JIRA_PASS) ? new ConfirmProjectPage() : new ChooseModulePage();
                    changePage(nextPage);
                }
            } catch (Exception e) {
                String msg = "Failed to communicate with JIRA.";
                LOG.debug(msg, e);
                area.setDisable(false);
                changePage(new ErrorPage(msg + "\n\nPlease verify your username and password and try again.", ConnectToJIRAPage.this));
            } finally {
                setBlocked(false);
            }
        });
    }
}
