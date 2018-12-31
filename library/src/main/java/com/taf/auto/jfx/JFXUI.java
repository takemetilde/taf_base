package com.taf.auto.jfx;

import com.taf.auto.common.HandleOptional;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.Optional;
import java.util.function.Predicate;

import static com.taf.auto.jfx.JFXThread.jfxSafeWait;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Convenience methods for working with JavaFX {@link Node}s and related constructs.
 *
 */
public final class JFXUI {
    private JFXUI() { /** static only */ }

    /**
     * Convenience method that calls {@link Node#getStyleClass()} to add the given styleClass.
     *
     * @param <N> the type of node
     * @param node       where to add the styleClass
     * @param styleClass the name of the style class
     * @return the given node
     */
    public static <N extends Node> N style(N node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }

    public static final class Factory {
        public static Label label(String styleClass) {
            return style(new Label(), styleClass);
        }

        public static Label label(String text, String styleClass) {
            return style(new Label(text), styleClass);
        }

        public static Button button(String text, String styleClass) {
            return button(text, styleClass, Optional.empty());
        }

        public static Button button(String text, String styleClass, EventHandler<ActionEvent> onAction) {
            Button button = style(new Button(text), styleClass);
            if(null != onAction)
                button.setOnAction(onAction);
            return button(text, styleClass, Optional.of(onAction));
        }

        private static Button button(String text, String styleClass, Optional<EventHandler<ActionEvent>> onAction) {
            Button button = style(new Button(text), styleClass);
            onAction.ifPresent(h -> button.setOnAction(h));
            return button;
        }
    }

    public static final class Alerts {
        private static Optional<ButtonType> dialog(String message, Alert.AlertType type, Node parent) {
            HandleOptional<ButtonType> result = new HandleOptional<>();
            jfxSafeWait(() -> {
                Alert alert = new Alert(type, message);
                alert.initOwner(parent.getScene().getWindow());
                Optional<ButtonType> buttonType = alert.showAndWait();
                result.setValue(buttonType);
            });
            return result.getValue();
        }

        public static void error(String message, Node parent) {
            dialog(message, ERROR, parent);
        }

        public static void info(String message, Node parent) {
            dialog(message, INFORMATION, parent);
        }

        /**
         * Shows a confirmation dialog.
         *
         * @param message the message to display
         * @param parent the parent to anchor above
         * @return {@code true} if Yes was pressed otherwise {@code false}
         */
        public static boolean confirm(String message, Node parent) {
            Optional<ButtonType> result = dialog(message, CONFIRMATION, parent);
            if(result.isPresent())
                return result.get() == ButtonType.OK;
            return false;
        }

        public static Predicate<ButtonType> WAS_OK = response -> response == ButtonType.OK;

        public static Optional<String> input(String message, Node parent) {
            return input(message, "", parent);
        }

        public static Optional<String> input(String message, String initialValue, Node parent) {
            return input(Optional.empty(), message, initialValue, parent);
        }

        public static Optional<String> input(String header, String message, String initialValue, Node parent) {
            return input(Optional.of(header), message, initialValue, parent);
        }

        private static Optional<String> input(Optional<String> optHeader, String message, String initialValue, Node parent) {
            HandleOptional<String> resultHandle = new HandleOptional<>();
            jfxSafeWait(() -> {
                TextInputDialog dlg = new TextInputDialog(initialValue);
                optHeader.ifPresent(h -> dlg.setHeaderText(h));
                dlg.setContentText(message);
                dlg.initOwner(parent.getScene().getWindow());
                dlg.showAndWait();
                String result = dlg.getResult();
                resultHandle.setValue(Optional.ofNullable(result));
            });
            return resultHandle.getValue();
        }
    }
}
