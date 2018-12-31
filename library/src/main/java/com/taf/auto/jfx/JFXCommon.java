package com.taf.auto.jfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;

import static com.taf.auto.jfx.JFXThread.jfxSafe;

public class JFXCommon {
    public static void setOnFirstSized(Node node, Runnable response) {
        node.boundsInLocalProperty().addListener(new SetOnFirstSizedAdapter(node, response));
    }
    
    private static class SetOnFirstSizedAdapter implements ChangeListener<Bounds> {
        private final Node node;
        private final Runnable response;

        private boolean fired = false;

        public SetOnFirstSizedAdapter(Node node, Runnable response) {
            this.node = node;
            this.response = response;
        }

        @Override
        public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
            if(fired)
                return;

            if(newValue.getWidth() != 0) {
                fired = true;
                node.boundsInLocalProperty().removeListener(this);
                jfxSafe(response, true);
            }
        }
    }
}
