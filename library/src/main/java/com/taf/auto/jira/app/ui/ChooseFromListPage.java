package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.JFXThread;
import com.taf.auto.jfx.JFXUI;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.taf.auto.jfx.JFXThread.jfxSafe;
import static com.taf.auto.jfx.JFXUI.style;

/**
 * Abstract UI for a page that presents a number of choices.
 *
 */
public abstract class ChooseFromListPage<C> extends ContinueOrCancelPage {
    private static final Logger LOG = LoggerFactory.getLogger(ChooseFromListPage.class);

    private ListView<C> listChoices;

    /**
     * Subclass must provide a short summary of the type of choice presented.
     *
     * @param optChoice the optional choice to summarize
     * @return the summary
     */
    protected abstract String peekChoiceSummary(Optional<C> optChoice);

    protected abstract List<C> peekChoices();

    protected abstract Optional<C> peekInitialChoice();

    @Override
    protected final Node buildCenter(Button buttonCancel, Button buttonContinue) {
        listChoices = new ListView<>();
        listChoices.setOnMousePressed(fireContinueOnDoubleClick());

        listChoices.getItems().addAll(peekChoices());
        peekInitialChoice().ifPresent(c -> listChoices.getSelectionModel().select(c));

        BooleanProperty noChoiceMade = new SimpleBooleanProperty(true);
        Label labelChoiceMade = JFXUI.Factory.label(peekChoiceSummary(Optional.empty()), "label-project");
        labelChoiceMade.textProperty().addListener((observable, oldValue, newValue) -> {
            noChoiceMade.set(listChoices.getSelectionModel().isEmpty());
        });

        listChoices.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> labelChoiceMade.setText(peekChoiceSummary(Optional.ofNullable(n))));

        buttonContinue.disableProperty().bind(noChoiceMade);

        VBox bottom = new VBox();
        bottom.setAlignment(Pos.CENTER);
        bottom.setFillWidth(true);
        bottom.getChildren().addAll(buildFinder(), labelChoiceMade);

        BorderPane pane = style(new BorderPane(listChoices), "pane-padded");
        BorderPane.setAlignment(labelChoiceMade, Pos.CENTER);
        pane.setBottom(bottom);
        return pane;
    }

    private Node buildFinder() {
        TextField textFinder = new TextField();
        textFinder.textProperty().addListener((o, old, neo) -> {
            String find = neo.trim().toLowerCase();
            if(!find.isEmpty()) {
                int i = 0;
                ObservableList<C> items = listChoices.getItems();
                for (int len = items.size(); i < len; i++) {
                    if (items.get(i).toString().toLowerCase().contains(find)) {
                        int target = i;
                        JFXThread.jfxSafe(() -> {
                            listChoices.getSelectionModel().select(target);
                            listChoices.scrollTo(target);
                        });
                        return;
                    }
                }
            }
            JFXThread.jfxSafe(() -> listChoices.getSelectionModel().clearSelection());
        });
        fireContinueOnEnter(textFinder);
        BorderPane pane = new BorderPane(textFinder);
        Label labelFinder = new Label("Finder: ");
        BorderPane.setAlignment(labelFinder, Pos.CENTER);
        pane.setLeft(labelFinder);

        jfxSafe(() -> textFinder.requestFocus(), true);

        return pane;
    }

    protected abstract void continueWithChoice(C choice);

    @Override
    protected final void handleContinue(ActionEvent ae) {
        C choice = listChoices.getSelectionModel().getSelectedItem();
        if(null != choice) {
            continueWithChoice(choice);
        } else {
            LOG.error("Choice unexpectedly null");
        }
    }
}
