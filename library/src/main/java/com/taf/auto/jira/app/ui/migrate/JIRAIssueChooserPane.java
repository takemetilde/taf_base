package com.taf.auto.jira.app.ui.migrate;

import com.taf.auto.jfx.JFXUI;
import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jfx.app.ui.UIRoot;
import com.taf.auto.jira.IssueTypes;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.rest.UserPass;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

import static com.taf.auto.common.PrettyPrinter.prettyArray;
import static com.taf.auto.common.PrettyPrinter.prettyException;
import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static java.lang.String.format;

/**
 * Widget for choosing an issue within a project.
 */
public class JIRAIssueChooserPane extends BorderPane {
    public static final String NO_ISSUE = "?";

    private final String projectKey;
    private final IssueTypes[] requiredIssueTypes;
    private final UIPage parent;
    private final Label labelTarget;

    private Optional<String> optKey;
    private final Button button;

    public JIRAIssueChooserPane(String label, String projectKey, UIPage parent, IssueTypes... issueTypes ) {
        this.projectKey = projectKey;
        this.requiredIssueTypes = issueTypes;
        this.parent = parent;

        optKey = Optional.empty();

        Label labelInstruction = new Label(label);
        BorderPane.setAlignment(labelInstruction, Pos.CENTER);

        labelTarget = new Label(NO_ISSUE);
        BorderPane.setAlignment(labelTarget, Pos.CENTER_LEFT);
        BorderPane.setMargin(labelTarget, new Insets(0, 4, 0, 4));

        button = new Button("Select...");
        button.setOnAction(this::onSelect);

        setCenter(labelTarget);
        setLeft(labelInstruction);
        setRight(button);
    }

    private void onSelect(ActionEvent evt) {
        Optional<String> result = JFXUI.Alerts.input("Specify User Story to Test", "JIRA Issue Key:", projectKey + "-", this)
                .map(r -> r.trim().toUpperCase());

        optKey = Optional.ofNullable(result.orElse(null));

        UIRoot root = parent.peekRoot();
        root.setBlocked(true);
        root.execute(this::verifySelectedIssue);
    }

    private void verifySelectedIssue() {
        if(optKey.isPresent()) {
            String key = optKey.get();
            UserPass creds = parent.peekRoot().peekSettings().peekCreds();
            try {
                IssueTypes issueType = JIRAUtil.fetchIssueType(key, creds);
                if(!isRequiredType(issueType)) {
                    JFXUI.Alerts.error(format("You selected a %s but you must select %s.", issueType, formatRequiredTypes()), this);
                    optKey = Optional.empty();
                }
            } catch (Throwable t) {
                JFXUI.Alerts.error("Unable to fetch Issue type for: " + key + "\n" + prettyException(t), this);
                optKey = Optional.empty();
            }
        }

        jfxSafe(() -> {
            labelTarget.setText(optKey.orElseGet(() -> NO_ISSUE));
            parent.peekRoot().setBlocked(false);
        });
    }

    private boolean isRequiredType(IssueTypes issueType) {
        for(IssueTypes type : requiredIssueTypes) {
            if(type == issueType) {
                return true;
            }
        }
        return false;
    }

    private String formatRequiredTypes() {
        if(requiredIssueTypes.length == 1) {
            return "a " + requiredIssueTypes[0];
        }
        return "one of: " + prettyArray(requiredIssueTypes);
    }

    public Optional<String> peekIssueKey() {
        return optKey;
    }

    public void fire() {
        button.fire();
    }
}
