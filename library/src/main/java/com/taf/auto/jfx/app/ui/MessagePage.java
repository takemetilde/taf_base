package com.taf.auto.jfx.app.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 * UI for showing a message.
 */
public abstract class MessagePage extends UIPage {
    private final String message;
    private final UIPage nextPage;

    public MessagePage(String message, UIPage nextPage) {
        this.message = message;
        this.nextPage = nextPage;
    }

    @Override
    protected final Node buildContent() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(message);

        BorderPane area = new BorderPane(textArea);

        if(null != nextPage) {
            Button buttonOK = new Button("OK");
            buttonOK.setOnAction(event -> changePage(nextPage));
            BorderPane.setAlignment(buttonOK, Pos.CENTER);
            area.setBottom(buttonOK);
        }

        return area;
    }
}
