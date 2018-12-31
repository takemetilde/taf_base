package com.taf.auto.jfx.app.ui;

import com.taf.auto.io.JSONUtil;
import com.taf.auto.jfx.JFXUI;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static com.taf.auto.IOUtil.readBytesFromClasspath;
import static com.taf.auto.common.PrettyPrinter.prettyException;
import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.JFXUI.Factory.label;
import static com.taf.auto.jfx.JFXUI.style;
import static com.taf.auto.jira.app.XrayUtilityApp.resolveResource;

/**
 * Root class for the UI. Provides a center area that shows the current {@link UIPage}.
 *
 */
public final class UIRoot {
    private static final Logger LOG = LoggerFactory.getLogger(UIRoot.class);

    private final Stage stage;
    private final BorderPane root;
    private final Label labelPageTitle;

    private final ExecutorService executor;

    private final UISettings settings;

    private final UIActivityLog log;
    private final Overlay overlay;
    private final Function<UIRoot, UIPage> initialPage;
    private final String appName;

    public UIRoot(Stage stage, Function<UIRoot, UIPage> initialPage, String appName) {
        this.appName = appName;
        LOG.info("Launched: " + appName);
        log = new UIActivityLog(appName);
        log.log("==== " + appName + " ====");
        settings = loadSettings();
        executor = Executors.newCachedThreadPool();

        this.stage = stage;
        labelPageTitle = buildTitle();
        root = new BorderPane(new Label("Initializing..."));

        BorderPane header = style(new BorderPane(labelPageTitle), "root-header");

        root.setTop(header);
        this.initialPage = initialPage;
        pokePage(initialPage.apply(this));

        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        StackPane stack = new StackPane();
        overlay = new Overlay();
        stack.getChildren().addAll(root, overlay);

        Scene scene = new Scene(stack);
        scene.getStylesheets().add(resolveResource("css/styles.css"));

        stage.setScene(scene);
        root.setPrefSize(600, 600);
        stage.setTitle(peekTitle());
        stage.show();
    }

    private static String peekTitle() {
        String version;
        try {
            version = new String(readBytesFromClasspath(resolveResource("version")));
        } catch (Exception e) {
            LOG.error("Failed to resolve version", e);
            version = "?";
        }
        return "Cucumber Xray Utility v" + version;
    }

    private class Overlay extends BorderPane {

        private final Label label;

        Overlay() {
            style(this, "root-overlay");
            label = JFXUI.Factory.label("", "label-please-wait");
            reset();
            setCenter(label);
            setVisible(false);
        }

        void setBlocked(boolean blocked) {
            setVisible(blocked);
            if(!blocked) {
                reset();
            }
        }

        void reset() {
            label.setText("Please wait...");
        }

        void poke(String label) {
            this.label.setText(label);
        }
    }

    public void setBlocked(boolean blocked) {
        jfxSafe(() -> overlay.setBlocked(blocked));
    }

    public void updateBlockedLabel(String label) {
        jfxSafe(() -> overlay.poke(label));
    }

    private static Label buildTitle() {
        Label label = label("label-title");
        BorderPane.setAlignment(label, Pos.CENTER);
        return label;
    }

    public void pokePage(UIPage page) {
        jfxSafe(() -> {
            page.pokeRoot(this);
            try {
                root.setCenter(page.peekContent());
            } catch (Exception e) {
                LOG.error("Failed to build page content: " + page.getClass(), e);
                root.setCenter(new Label("Failed to build page content, please see log."));
            }
            labelPageTitle.setText(page.peekTitle());
        });
    }

    public Node peekDialogAnchor() {
        return root;
    }

    public Stage peekStage() {
        return stage;
    }

    /**
     * Executes the given logic on a non-GUI thread. This uses a single threaded executor.
     * This does not block the calling thread. The given future should handle any GUI impact
     * of succeeding (or failing).
     *
     * @param logic the logic to run
     */
    public void execute(Runnable logic) {
        executor.execute(() -> {
            try {
                logic.run();
            } catch (Throwable t) {
                String msg = "Uncaught exception in executable logic!";
                LOG.error(msg, t);
                UIPage page = initialPage.apply(this);
                pokePage(new ErrorPage(msg + "\n" + prettyException(t), page));
            }
        });
    }

    private File peekSettingsFile() {
        return new File(System.getProperty("user.home"), appName + ".json");
    }

    private UISettings loadSettings() {
        File file = peekSettingsFile();

        if (file.exists()) {
            try {
                return JSONUtil.decode(file, UISettings.class);
            } catch (IOException ioe) {
                LOG.error("Failed to load settings from: " + file, ioe);
                return new UISettings();
            }
        } else {
            LOG.info("No settings file found: " + file);
            return new UISettings();
        }
    }

    public UISettings peekSettings() {
        return settings;
    }

    void saveSettings() {
        File file = peekSettingsFile();
        try {
            JSONUtil.encode(file, settings);
        } catch (IOException ioe) {
            LOG.error("Failed to save settings to: " + file, ioe);
        }
    }

    void logActivity(String activity) {
        log.log(activity);
    }
}
