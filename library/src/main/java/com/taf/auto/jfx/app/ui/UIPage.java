package com.taf.auto.jfx.app.ui;

import com.taf.auto.jfx.JFXThread;
import com.taf.auto.jfx.JFXUI;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Optional;

import static com.taf.auto.common.PrettyPrinter.prettyException;
import static java.lang.String.format;

/**
 * Abstract parent of UI pages.
 *
 */
public abstract class UIPage {
    private UIRoot root;

    private Optional<Node> content;

    public UIPage() {
        /** root will be passed in by {@link UIRoot} */
        root = null;
        content = Optional.empty();
    }

    /**
     * Called by {@link UIRoot} when this page is added.
     * @param root
     */
    final void pokeRoot(UIRoot root) {
        this.root = root;
    }

    public final UIRoot peekRoot() {
        return root;
    }

    protected final Node peekDialogAnchor() {
        return root.peekDialogAnchor();
    }

    protected final Stage peekStage() {
        return root.peekStage();
    }

    protected final void changePage(UIPage page) {
        root.pokePage(page);
    }

    protected void execute(Runnable logic) {
        root.execute(logic);
    }

    protected final void logActivity(String activity) {
        root.logActivity(activity);
    }

    protected UISettings peekSettings() {
        return root.peekSettings();
    }

    protected void saveSettings() {
        root.saveSettings();
    }

    protected abstract String peekTitle();

    public final String getTitle() {
        return peekTitle();
    }

    /**
     * Obtains the content for this page. The first time this method is called it will invoke {@link #buildContent()}
     * and return it. Subsequent calls will return the originally created value.
     *
     * @return the content for this page
     */
    public final Node peekContent() {
        JFXThread.throwIfNotJFXThread();
        if(!content.isPresent()) {
            content = Optional.of(buildContent());
        }
        return content.get();
    }

    protected abstract Node buildContent();

    protected Pane buildConsole(Node... children) {
        FlowPane console = new FlowPane(children);
        console.setAlignment(Pos.CENTER);
        console.setHgap(2);
        console.setPadding(new Insets(2));
        return console;
    }

    protected final void setBlocked(boolean blocked) {
        root.setBlocked(blocked);
    }

    protected final void updateBlockedLabel(String label) {
        root.updateBlockedLabel(label);
    }

    protected final void showError(String msg) {
        JFXUI.Alerts.error(msg, peekDialogAnchor());
    }

    protected final void showError(String msg, Throwable t) {
        String message = prettyException(t);
        JFXUI.Alerts.error(format("%s: %s", msg, message), peekDialogAnchor());
    }

    protected final boolean showConfirmation(String msg) {
        return JFXUI.Alerts.confirm(msg, peekDialogAnchor());
    }
}
