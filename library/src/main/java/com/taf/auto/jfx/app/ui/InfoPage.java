package com.taf.auto.jfx.app.ui;

/**
 * UI for showing a standard info message.
 *
 */
public class InfoPage extends MessagePage {
    public InfoPage(String message, UIPage nextPage) {
        super(message, nextPage);
    }

    @Override
    protected String peekTitle() {
        return "Info";
    }
}