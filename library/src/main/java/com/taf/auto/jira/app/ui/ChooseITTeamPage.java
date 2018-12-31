package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jfx.app.ui.UISettings;
import com.taf.auto.jira.ITTeams;

import java.util.List;
import java.util.Optional;

/**
 * Created by AF04261 on 7/25/2017.
 */
public class ChooseITTeamPage extends ChooseFromListPage<ITTeams> {
    private final ChooseProjectPage parent;
    private final List<ITTeams> values;

    public ChooseITTeamPage(ChooseProjectPage parent, List<ITTeams> values) {
        this.parent = parent;
        this.values = values;
    }

    protected UIPage defineCancelPage() {
        return parent;
    }

    @Override
    protected String peekChoiceSummary(Optional<ITTeams> optChoice) {
        return optChoice.isPresent() ? "IT Team: " + optChoice.get() : "<Please select your IT Team>";
    }

    @Override
    protected List<ITTeams> peekChoices() {
        return values;
    }

    @Override
    protected Optional<ITTeams> peekInitialChoice() {
        ITTeams memory = peekSettings().peek(ITTeams.class);

        for(ITTeams v : values) {
            if(v.id == memory.id) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    @Override
    protected void continueWithChoice(ITTeams choice) {
        UISettings settings = peekSettings();
        settings.poke(choice);
        logActivity("Selected IT Team: " + choice);
        saveSettings();
        changePage(new ChooseModulePage());
    }

    @Override
    protected String peekTitle() {
        return "Choose IT Team";
    }
}
