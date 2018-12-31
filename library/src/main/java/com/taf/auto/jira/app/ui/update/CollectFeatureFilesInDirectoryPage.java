package com.taf.auto.jira.app.ui.update;

import com.taf.auto.io.JSONUtil;
import com.taf.auto.jfx.app.ui.ErrorPage;
import com.taf.auto.jfx.app.ui.InfoPage;
import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jira.app.io.FeatureFileVisitor;
import com.taf.auto.jira.pojo.xray.XrayTest;
import com.taf.auto.jira.xray.ScenarioBundle;
import com.taf.auto.jira.xray.ScenarioForXray;
import com.taf.auto.rest.UserPass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.app.ui.UIFactory.Lbl.pleaseWait;
import static com.taf.auto.jira.JIRAUtil.fetchIssue;
import static com.taf.auto.jira.xray.ScenarioForXray.parseScenarios;
import static java.lang.String.format;

public class CollectFeatureFilesInDirectoryPage extends UIPage {
    private static final Logger LOG = LoggerFactory.getLogger(CollectFeatureFilesInDirectoryPage.class);

    private final Path dir;
    private Label label;

    private boolean ignoredOrSuccess;

    public CollectFeatureFilesInDirectoryPage(Path dir) {
        this.dir = dir;
    }

    @Override
    protected String peekTitle() {
        return "Review Feature Files";
    }

    @Override
    protected Node buildContent() {
        execute(this::collect);
        label = pleaseWait();
        return label;
    }

    private void updateLabel(String key, int num, int max) {
        String txt = String.format("Collecting %s (%d/%d)...", key, num, max);
        jfxSafe(() -> label.setText(txt));
    }

    private void collect() {
        List<CollectFeatureFileError> errors = new ArrayList<>();
        try {
            UserPass creds = peekSettings().peekCreds();

            String projectKey = peekSettings().projectKey;
            List<Path> files = FeatureFileVisitor.collectScenarios(projectKey, dir);
            LOG.info(format("Found %d feature files in: %s", files.size(), dir));

            List<ScenarioBundle> allScenarios = new ArrayList<>();

            AtomicInteger i = new AtomicInteger(1);
            int max = files.size();
            ignoredOrSuccess = false;
            files.forEach(f -> {
                String key = null;
                try {
                    key = extractKey(f);

                    IssueUpdateStatus status = StatusCache.peek(key);
                    switch (status) {
                        case Ignore: LOG.info("Ignoring: " + key); ignoredOrSuccess = true; return;
                        case Success: LOG.info("Already successful: " + key); ignoredOrSuccess = true; return;
                        case Closed: LOG.info("Closed: " + key); ignoredOrSuccess = true; return;
                    }

                    List<ScenarioForXray> scenarios = parseScenarios(f.toFile(), projectKey);
                    int numScenarios = scenarios.size();
                    LOG.debug(format("Found %d scenarios for: %s", numScenarios, f));
                    if(numScenarios > 1) {
                        throw new Exception(f.toString() + " contains more than one Scenario");
                    }

                    ScenarioForXray scenario = scenarios.get(0);

                    updateLabel(key, i.getAndIncrement(), max);
                    byte[] data = fetchIssueData(key, creds);
                    XrayTest issue = JSONUtil.decode(data, XrayTest.class);
                    LOG.info("Fetched: " + issue.key);

                    allScenarios.add(new ScenarioBundle(f, scenario, issue));
                } catch (Exception e) {
                    String msg = "Failed to process: " + f;
                    LOG.error(msg, e);
                    errors.add(new CollectFeatureFileError(key, f, e.getMessage()));
                }
            });

            UIPage nextPage;
            if(!errors.isEmpty()) {
                nextPage = new ReviewCollectionErrorsPage(errors, allScenarios);
            } else if(allScenarios.isEmpty()) {
                String msg = ignoredOrSuccess ? "All Xray scenario files in this directory have already been processed." :
                        "This directory does not contain any Xray scenario files.";
                nextPage = new InfoPage(msg, new UpdateAllXrayTestsInDirectoryPage());
            } else {
                nextPage = new CommitFeatureFilesInDirectoryPage(allScenarios);
            }
            changePage(nextPage);

        } catch (Exception e) {
            String msg = "Failed to collect Scenarios";
            LOG.error(msg, e);
            changePage(new ErrorPage(msg + "\n" + e.getMessage(), new UpdateAllXrayTestsInDirectoryPage()));
        }
    }

    /**
     * Loads the byte[] for the given issue from JIRA and cache it for future. If a cached version is found, that
     * data will be returned and JIRA will not get queried.
     *
     * @param key
     * @return
     */
    static byte[] fetchIssueData(String key, UserPass creds) throws IOException {
        Optional<byte[]> cached = IssueCache.peek(key);
        if(cached.isPresent())
            return cached.get();
        byte[] data = fetchIssue(key, creds.username, creds.password);
        IssueCache.poke(key, data);
        return data;
    }

    private static List<String> extractTags(String[] originalTags) {
        List<String> tags = new ArrayList<>(originalTags.length);
        for(String tag : originalTags) {
            tag = tag.trim();
            if(!tag.isEmpty())
                tags.add(tag);
        }
        return tags;
    }

    private static String extractKey(Path f) {
        String filename = f.getFileName().toString();
        String substring = filename.substring(0, filename.indexOf('.'));
        int at = substring.indexOf("_");
        if(at > -1) {
            LOG.warn("Cleansed unexpected characters from: " + substring);
            substring = substring.substring(0, at);
        }
        return substring;
    }
}
