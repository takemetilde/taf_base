package com.taf.auto.jira.app.ui;

import com.taf.auto.jfx.app.ui.UIPage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by AF04261 on 4/28/2017.
 */
public abstract class ContinuePage extends UIPage {
    private static final Logger LOG = LoggerFactory.getLogger(ContinuePage.class);

    private Button buttonContinue;

    protected abstract Node buildCenter(Button buttonContinue);

    @Override
    protected final Node buildContent() {
        buttonContinue = new Button("Continue");
        buttonContinue.setOnAction(this::handleContinue);

        Node center;
        try {
            center = buildCenter(buttonContinue);
        } catch (Exception e) {
            LOG.error("Unable to build content", e);
            center = new Label("Unexpected error building content, please see log.");
        }
        BorderPane.setAlignment(center, Pos.CENTER);
        BorderPane pane = new BorderPane(center);
        List<Node> bottomNodes = buildBottom();
        pane.setBottom(buildConsole(bottomNodes.toArray(new Node[bottomNodes.size()])));

        return pane;
    }

    protected List<Node> buildBottom() {
        return Collections.singletonList(buttonContinue);
    }

    protected UIPage defineCancelPage() {
        return new ChooseModulePage();
    }

    protected abstract void handleContinue(ActionEvent ae);

    protected EventHandler<ActionEvent> fireContinue() {
        return e -> buttonContinue.fire();
    }

    protected void fireContinueOnEnter(Node node) {
        fireContinueOnEnter(node, ke -> {});
    }

    protected void fireContinueOnEnter(Node node, Consumer<KeyEvent> handleAdditional) {
        node.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buttonContinue.fire();
            } else {
                handleAdditional.accept(e);
            }
        });
    }

    protected EventHandler<MouseEvent> fireContinueOnDoubleClick() {
        return e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) buttonContinue.fire();
        };
    }
}
