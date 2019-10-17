package com.taf.auto.jira.app.ui;

import com.taf.auto.IOUtil;
import com.taf.auto.jfx.app.ui.UIPage;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.taf.auto.IOUtil.peekWorkingDirectory;
import static com.taf.auto.jfx.JFXCommon.setOnFirstSized;
import static java.lang.String.format;

/**
 * UI for choosing a file or directory and taking an action.
 *
 */
public abstract class ChooseFileOrDirectoryPage extends UIPage {
    private static final Logger LOG = LoggerFactory.getLogger(ChooseFileOrDirectoryPage.class);

    private final String name;
    private final Mode mode;
    private final Supplier<String> titleProvider;

    public enum Mode {
        File, Directory
    }

    public ChooseFileOrDirectoryPage(String name, Mode mode) {
        this(name, mode, () -> format("Choose %s %s", name, mode));
    }

    public ChooseFileOrDirectoryPage(String name, Mode mode, String title) {
        this(name, mode, () -> title);
    }

    public ChooseFileOrDirectoryPage(String name, Mode mode, Supplier<String> titleProvider) {
        this.name = name;
        this.mode = mode;
        this.titleProvider = titleProvider;
    }

    protected abstract String peekMemoryPath();

    protected abstract void acceptFile(File file);

    @Override
    protected String peekTitle() {
        return titleProvider.get();
    }

    @Override
    protected Node buildContent() {
        Button buttonBrowse = new Button("Browse...");
        buttonBrowse.setOnAction(this::browseForFile);

        Button buttonBack = new Button("Back");
        buttonBack.setOnAction(e -> changePage(new ChooseModulePage()));
        BorderPane.setAlignment(buttonBack, Pos.CENTER);

        BorderPane area = new BorderPane();
        area.setCenter(buttonBrowse);
        area.setBottom(buttonBack);

        setOnFirstSized(buttonBrowse, () -> buttonBrowse.fire());

        return area;
    }

    private File peekInitialDirectory() {
        try {
            String path = peekMemoryPath();
            if (null != path && !path.isEmpty()) {
                try {
                    File dir = new File(path);
                    if(mode == Mode.File)
                        dir = dir.getParentFile();
                    for (; null != dir; dir = dir.getParentFile()) {
                        if (dir.isDirectory()) {
                            LOG.debug("Found initial directory: " + dir);
                            return dir;
                        }
                        LOG.debug("Initial directory not present, trying parent: " + dir);
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to resolve previous path: " + path);
                }
            }
        } catch(Exception e) {
            LOG.error("Unexpected error determining initial directory", e);
        }

        return peekWorkingDirectory();
    }

    private void browseForFile(ActionEvent event) {
        browseForFile(name, mode, Optional.of(peekInitialDirectory()), this::acceptFile, peekStage());
    }

    public static void browseForFile(String name, Mode mode, Optional<File> optInitialDirectory, Consumer<File> acceptor, Window ownerWindow) {
        String title = format("Open %s %s", name, mode);

        File initialDirectory = optInitialDirectory.orElseGet(IOUtil::peekWorkingDirectory);

        File file;
        if(mode == Mode.File) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(initialDirectory);
            fileChooser.setTitle(title);
            file = fileChooser.showOpenDialog(ownerWindow);
            LOG.info("Browsed to file: " + file);
        } else { // Mode.Directory
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(initialDirectory);
            chooser.setTitle(title);
            file = chooser.showDialog(ownerWindow);
            LOG.info("Browsed to directory: " + file);
        }
        if(null != file) {
            try {
                acceptor.accept(file);
            } catch(Exception e) {
                LOG.error("Unexpected exception accepting file: " + file.getAbsolutePath());
            }
        }
    }
}
