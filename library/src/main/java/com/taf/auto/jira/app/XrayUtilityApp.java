package com.taf.auto.jira.app;

import com.taf.auto.jfx.app.ui.UIPage;
import com.taf.auto.jfx.app.ui.UIRoot;
import com.taf.auto.jira.app.ui.ConnectToJIRAPage;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static java.lang.String.format;

/**
 * Tool for working with Xray and feature files.
 *
 * @author AF04261 mmorton
 */
public class XrayUtilityApp extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(XrayUtilityApp.class);

    public void start(Stage primaryStage) throws Exception {
        establishIcon(primaryStage);
        new UIRoot(primaryStage, this::defineFirstPage, XrayUtilityApp.class.getSimpleName());
    }

    private void establishIcon(Stage stage) {
        int[] sizes = {16, 32, 48, 256};
        ObservableList<Image> icons = stage.getIcons();
        for(int size : sizes) {
            String path = format(resolveResource("img/anthem-logo%d.png"), size);
            try {
                URL url = getClass().getResource(path);
                if(null == url)
                    throw new IOException("Icon not found");
                Image image = new Image(url.openStream());
                icons.add(image);
            } catch (Exception e) {
                LOG.error("Failed to load application icon: " + path, e);
            }
        }
    }

    private UIPage defineFirstPage(UIRoot root) {
        return new ConnectToJIRAPage();
    }

    public static String resolveResource(String name) {
        return "/jiraapp/" + name;
    }

    public static void main(String[] args) {
        Application.launch(XrayUtilityApp.class, args);
    }
}
