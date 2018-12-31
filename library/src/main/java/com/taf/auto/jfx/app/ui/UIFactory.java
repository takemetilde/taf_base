package com.taf.auto.jfx.app.ui;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import static com.taf.auto.jfx.JFXUI.style;

public class UIFactory {
    private UIFactory() { /** static only */ }

    public static class Lbl {
        public static Label pleaseWait() {
            return new Label("Please wait...");
        }

        public static BorderPane labeled(String labelText, Node subject) {
            Label label = new Label(labelText);
            BorderPane.setAlignment(label, Pos.CENTER);

            BorderPane pane = new BorderPane(subject);
            pane.setLeft(label);

            return pane;
        }

        public static GridPane labeled(String[] labels, Node[] subjects) {
            if(labels.length != subjects.length) {
                throw new IllegalArgumentException("number of labels must match number of subjects");
            }

            GridPane pane = style(new GridPane(), "pane-labeled");

            for(int i = 0; i < labels.length; i++) {
                Label label = new Label(labels[i]);
                GridPane.setHalignment(label, HPos.RIGHT);
                pane.add(label, 0, i);
                GridPane.setHgrow(subjects[i], Priority.ALWAYS);
                pane.add(subjects[i], 1, i);
            }

            return pane;
        }
    }

    public static class Pn {
        public static BorderPane centered(Node subject) {
            BorderPane.setAlignment(subject, Pos.CENTER);
            return new BorderPane(subject);
        }
    }
}
