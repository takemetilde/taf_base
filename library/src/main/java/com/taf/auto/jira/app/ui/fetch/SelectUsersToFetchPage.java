package com.taf.auto.jira.app.ui.fetch;

import com.taf.auto.jfx.JFXThread;
import com.taf.auto.jfx.app.ui.UserCancelException;
import com.taf.auto.jira.app.ui.ContinueOrCancelPage;
import com.taf.auto.jira.pojo.xray.XrayPreCondition;
import com.taf.auto.jira.pojo.xray.XrayTest;
import com.taf.auto.jira.xray.XrayUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.taf.auto.jfx.JFXUI.Factory.label;
import static com.taf.auto.jfx.JFXUI.style;
import static java.lang.String.format;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

/**
 * Page to select from available Users to download for a Test.
 */
public class SelectUsersToFetchPage extends ContinueOrCancelPage {
    private static final Logger LOG = LoggerFactory.getLogger(SelectUsersToFetchPage.class);

    private final XrayFetchPage parent;
    private final XrayTest test;
    private TableView<XrayPreCondition> table;

    private boolean isCanceled;

    public SelectUsersToFetchPage(XrayFetchPage parent, XrayTest test) {
        this.parent = parent;
        this.test = test;
        isCanceled = false;
    }

    @Override
    protected String peekTitle() {
        return "Select Users";
    }

    @Override
    protected Node buildCenter(Button buttonCancel, Button buttonContinue) {
        table = new TableView<>();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fireContinueOnEnter(table);

        TableColumn<XrayPreCondition, String> colKey = new TableColumn<>("Key");
        colKey.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().key));

        TableColumn<XrayPreCondition, String> colSummary = new TableColumn<>("Summary");
        colSummary.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().fields.summary));

        table.getColumns().addAll(colKey, colSummary);

        BooleanProperty noUserSelected = new SimpleBooleanProperty(true);
        ObservableList<XrayPreCondition> selectedItems = table.getSelectionModel().getSelectedItems();
        selectedItems.addListener((ListChangeListener) c -> noUserSelected.set(selectedItems.isEmpty()));
        buttonContinue.disableProperty().bind(noUserSelected);

        execute(this::loadUsers);

        BorderPane pane = style(new BorderPane(table), "pane-padded");
        pane.setBottom(label(format("Select desired user(s) for\n%s: %s", test.key, test.fields.summary), "label-choose-module-title"));
        return pane;
    }

    private void loadUsers() {
        List<XrayPreCondition> users;
        try {
            users = XrayUtil.PreCondition.fetchUsers(test, peekSettings().peekCreds());
        } catch (IOException e) {
            showError("Failed to load Users for Test: " + test.key);
            releaseParent();
            return;
        }

        JFXThread.jfxSafe(() -> {
            table.getItems().addAll(users);
            table.getSelectionModel().select(0);
        });

        setBlocked(false);
    }

    @Override
    protected void handleContinue(ActionEvent ae) {
        releaseParent();
    }

    private void releaseParent() {
        setBlocked(true);
        changePage(parent);
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Blocks the calling thread until Continue is pressed.
     *
     * @return the selected users.
     */
    List<XrayPreCondition> peekSelectedUsers() {
        JFXThread.throwIfJFXThread();
        LOG.debug("Blocking calling thread until User presses cancel or continue");
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException("peekSelectedUsers interrupted", e);
            }
        }

        if(isCanceled) {
            throw new UserCancelException();
        }

        return new ArrayList<>(table.getSelectionModel().getSelectedItems());
    }

    protected void handleCancel(ActionEvent ae) {
        isCanceled = true;
        releaseParent();
    }
}
