package com.taf.auto.common;

/**
 * The types of browsers supported by {@link com.taf.auto.WebDriverUtil}.
 */
public enum BrowserTypes {
    IE, CHROME, SAFARI, FIREFOX, MOBILE_DEVICE(true), MOBILE_CONFIG(true);

    public final boolean isMobile;

    BrowserTypes() {
        this(false);
    }

    BrowserTypes(boolean isMobile) {
        this.isMobile = isMobile;
    }
}
