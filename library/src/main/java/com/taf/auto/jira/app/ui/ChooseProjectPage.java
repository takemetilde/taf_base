package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jira.ITTeams;
import com.taf.auto.jira.JIRAUtil;
import javafx.scene.control.ChoiceBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * UI for choosing a project.
 *
 * @author AF04261 mmorton
 */
public class ChooseProjectPage extends ChooseFromListPage<String> {
    private static final Logger LOG = LoggerFactory.getLogger(ChooseProjectPage.class);

    private final List<String> projectKeys;
    private ChoiceBox<ITTeams> chooseTeam;

    public ChooseProjectPage(List<String> projectKeys) {
        this.projectKeys = projectKeys;
    }

    @Override
    protected String peekTitle() {
        return "Choose Project";
    }

    protected void continueWithChoice(String choice)
    {
        UISettings settings = peekSettings();
        settings.projectKey = choice;
        logActivity("Selected project: " + choice);
        saveSettings();
        setBlocked(true);
        updateBlockedLabel(format("Fetching IT Teams for %s...", choice));
        execute(() -> {
            try {
                try {
                    List<ITTeams> values = JIRAUtil.ITTeam.getAll(choice, settings.peekCreds());
                    if(values.isEmpty()) {
                        showError("Your User does not appear to have access to the Project: " + choice);
                    } else {
                        changePage(new ChooseITTeamPage(ChooseProjectPage.this, values));
                    }
                } catch (IOException e) {
                    String msg = "Failed to get all IT Teams";
                    LOG.error(msg, e);
                    showError(msg, e);
                }
            } finally {
                setBlocked(false);
            }
        });
    }

    protected UIPage defineCancelPage() {
        return new ConnectToJIRAPage();
    }

    @Override
    protected String peekChoiceSummary(Optional<String> optChoice) {
        return optChoice.isPresent() ? "Project: " + optChoice.get() : "<Please select a Project>";
    }

    @Override
    protected List<String> peekChoices() {
        return projectKeys;
    }

    @Override
    protected Optional<String> peekInitialChoice() {
        String key = peekSettings().projectKey;
        return !key.isEmpty() ? Optional.of(key) : Optional.empty();
    }
}
