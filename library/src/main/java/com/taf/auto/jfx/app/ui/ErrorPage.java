package com.taf.auto.jfx.app.ui;

/**
 * UI for showing a standard error message.
 *
 */
public class ErrorPage extends MessagePage {
    public ErrorPage(String message, UIPage nextPage) {
        super(message, nextPage);
    }

    @Override
    protected String peekTitle() {
        return "Error";
    }
}
