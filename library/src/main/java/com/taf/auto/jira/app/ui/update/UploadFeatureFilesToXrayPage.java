package com.taf.auto.jira.app.ui.update;

import com.taf.auto.io.JSONUtil;
import com.taf.auto.jfx.JFXUI;
import com.taf.auto.jfx.app.ui.ErrorPage;
import com.taf.auto.jfx.app.ui.InfoPage;
import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jira.ITTeams;
import com.taf.auto.jira.IssueHandle;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.pojo.xray.XrayTest;
import com.taf.auto.jira.xray.ScenarioBundle;
import com.taf.auto.jira.xray.ScenarioForXray;
import com.taf.auto.jira.xray.XrayUtil;
import com.taf.auto.rest.UserPass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.taf.auto.common.RunSafe.runWithRetries;
import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.app.ui.UIFactory.Lbl.pleaseWait;
import static com.taf.auto.jira.JIRAUtil.isIssueClosed;
import static com.taf.auto.jira.app.ui.update.CollectFeatureFilesInDirectoryPage.fetchIssueData;

/**
 * Page that uploads the Scenarios to Xray.
 *
 */
public class UploadFeatureFilesToXrayPage extends UIPage {
    private static final Logger LOG = LoggerFactory.getLogger(UploadFeatureFilesToXrayPage.class);

    private final List<ScenarioBundle> allScenarios;
    private Label labelProgress;
    private Label labelErrors;

    public UploadFeatureFilesToXrayPage(List<ScenarioBundle> allScenarios) {
        this.allScenarios = allScenarios;
    }

    @Override
    protected String peekTitle() {
        return "Uploading to Xray";
    }

    @Override
    protected Node buildContent() {
        execute(this::upload);
        labelProgress = pleaseWait();
        labelErrors = new Label();

        BorderPane pane = new BorderPane(labelProgress);
        pane.setBottom(labelErrors);

        return labelProgress;
    }

    private void updateProgress(String key, int num, int max) {
        String txt = String.format("Uploading %s (%d/%d)...", key, num, max);
        jfxSafe(() -> labelProgress.setText(txt));
    }

    /**
     * Caches issues may be flushed if errors occur. Replenish them.
     */
    private void replenishCache(UserPass creds) {
        allScenarios.forEach(s -> {
            String key = s.peekKey();
            Optional<byte[]> data = IssueCache.peek(key);
            if(!data.isPresent()) {
                LOG.info("Re-caching: " + key);
                try {
                    byte[] bytes = fetchIssueData(key, creds);
                    XrayTest issue = JSONUtil.decode(bytes, XrayTest.class);
                    s.issue = issue;
                } catch (IOException e) {
                    String msg = "Failed to re-cache: " + key;
                    LOG.error(msg, e);
                    JFXUI.Alerts.error(msg, peekDialogAnchor());
                }
            }
        });
    }

    private static class IssueClosedException extends Exception {
        IssueClosedException(String message) {
            super(message);
        }
    }

    private void upload() {
        List<UploadFeatureError> errors = new ArrayList<>();
        UserPass creds = peekSettings().peekCreds();
        try {
            replenishCache(creds);

            int uploadCount = 0;

            for(int i = 0, max = allScenarios.size(); i < max; i++) {
                ScenarioBundle item = allScenarios.get(i);
                String key = item.peekKey();
                updateProgress(key, i + 1, max);

                try {
                    if(isIssueClosed(item.issue)) {
                        throw new IssueClosedException("Test: " + item.peekKey() + " is Closed and cannot be updated. If necessary, open the Test in JIRA and try again.");
                    }
                    boolean wasUploaded = upload(item, creds);
                    if(wasUploaded) uploadCount++;
                    StatusCache.poke(key, IssueUpdateStatus.Success);
                } catch (Throwable t) {
                    LOG.error("Failed to upload: " + item, t);
                    errors.add(new UploadFeatureError(item, t.getMessage()));
                    if(t instanceof IssueClosedException) {
                        StatusCache.poke(key, IssueUpdateStatus.Closed);
                    } else {
                        StatusCache.poke(key, IssueUpdateStatus.Error);
                        IssueCache.clear(key);
                    }
                    jfxSafe(() -> labelErrors.setText("Errors: " + errors.size()));
                }
            }

            changePage(errors.isEmpty() ?
                    new InfoPage(String.format("%d of %d Scenarios uploaded to JIRA.", uploadCount, allScenarios.size()),
                            new UpdateAllXrayTestsInDirectoryPage()) :
                    new ReviewUploadErrorsPage(errors));

        } catch (Throwable t) {
            String msg = "Failed to upload";
            LOG.error(msg, t);
            changePage(new ErrorPage(msg + "\n" + t.getMessage(), new UpdateAllXrayTestsInDirectoryPage()));
        }
    }

    private boolean upload(ScenarioBundle<XrayTest> item, UserPass creds) {
        XrayTest issue = item.issue;
        LOG.info("Uploading: " + issue.key);
        ScenarioForXray scenario = item.scenario;

        IssueHandle handle = new IssueHandle(issue);
        ITTeams team = peekSettings().peek(ITTeams.class);

        boolean wasUpdated = false;

        if(!isScenarioEqual(item)) {
            runWithRetries(() -> updateScenario(scenario, handle, creds), 3);
            wasUpdated = true;
        } else {
            LOG.info("Scenario equal, skipping update");
        }

        if(!areLabelsSubset(item)) {
            runWithRetries(() -> updateLabels(scenario, issue.fields.labels, handle, creds), 3);
            wasUpdated = true;
        } else {
            LOG.info("Labels are subset, skipping update");
        }

        if(!isITTeamEquals(item, team)) {
            runWithRetries(() -> updateITTeam(team, handle, creds), 3);
            wasUpdated = true;
        } else {
            LOG.info("ITTeam equal, skipping update");
        }

        if(wasUpdated) {
            IssueCache.clear(issue.key);
        }

        LOG.info(wasUpdated ? "Upload complete: " + issue.key : "No upload necessary: " + issue.key);
        return wasUpdated;
    }

    private static boolean isScenarioEqual(ScenarioBundle<XrayTest> item) {
        if(null == item.issue.fields.cucumberTestType || null == item.issue.fields.cucumberScenario)
            return false;
        return item.issue.fields.cucumberTestType.value.equals(item.scenario.getType()) &&
                item.issue.fields.cucumberScenario.equals(item.scenario.getContent());
    }

    /**
     * Determine if all the existing labels exist for the proposed labels.
     * @param item the item to test
     * @return whether the labels are satisfied
     */
    private static boolean areLabelsSubset(ScenarioBundle<XrayTest> item) {
        for(String newLabel : item.scenario.getTags()) {
            boolean hit = false;
            for(String existingLabel : item.issue.fields.labels) {
                if(existingLabel.equals(newLabel)) {
                    hit = true;
                    break;
                }
            }
            if(!hit) return false;
        }
        return true;
    }

    private static boolean isITTeamEquals(ScenarioBundle<XrayTest> item, ITTeams newTeam) {
        if(null == item.issue.fields.IT_Team)
            return false;
        return item.issue.fields.IT_Team.length == 1 &&
                item.issue.fields.IT_Team[0].id.equals(Integer.toString(newTeam.id));
    }

    private void updateScenario(ScenarioForXray scenario, IssueHandle handle, UserPass creds) {
        LOG.info("Updating scenario");
        XrayUtil.updateScenario(scenario, handle, creds);
    }

    private void updateLabels(ScenarioForXray scenario, String[] labels, IssueHandle handle, UserPass creds) {
        LOG.info("Updating labels");
        List<String> labelsToAdd = new ArrayList<>(Arrays.asList(scenario.getTags()));

        for(String existingLabel : labels) {
            if(labelsToAdd.contains(existingLabel)) {
                LOG.debug("Label already present: " + existingLabel);
                labelsToAdd.remove(existingLabel);
            }
            else {
                LOG.debug("Label will be left alone: " + existingLabel);
            }
        }

        JIRAUtil.Label.add(handle, labelsToAdd, creds);
    }

    private void updateITTeam(ITTeams team, IssueHandle handle, UserPass creds) {
        LOG.info("Updating IT Team");
        JIRAUtil.ITTeam.update(handle, team, creds);
    }
}
