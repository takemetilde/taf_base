package com.taf.auto.jira.app.ui.migrate;

import com.taf.auto.jfx.app.ui.ErrorPage;
import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jira.xray.ScenarioForXray;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static com.taf.auto.jfx.app.ui.UIFactory.Lbl.pleaseWait;
import static com.taf.auto.jira.xray.ScenarioForXray.parseScenarios;

/**
 * UI for parsing the chosen feature file.
 *
 * @author AF04261 mmorton
 */
public class ParseFeatureFilePage extends UIPage {
    private static final Logger LOG = LoggerFactory.getLogger(ParseFeatureFilePage.class);

    private final File file;

    public ParseFeatureFilePage(File file) {
        this.file = file;
    }

    @Override
    protected String peekTitle() {
        return "Parsing Feature File";
    }

    @Override
    protected Node buildContent() {
        execute(this::parse);
        return pleaseWait();
    }

    private void parse() {
        try {
            List<ScenarioForXray> scenarios = parseScenarios(file, peekSettings().projectKey);
            changePage(new PostParseReviewPage(scenarios));

        } catch(Exception e) {
            LOG.error("Failed to parse", e);
            changePage(new ErrorPage(e.getMessage(), new ProcessNonXrayFeatureFile()));
        }
    }
}
