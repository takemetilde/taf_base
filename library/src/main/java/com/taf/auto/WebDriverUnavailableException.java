package com.taf.auto;

import com.taf.auto.common.BrowserTypes;

/**
 * Exception thrown when {@link WebDriverUtil} is unable to create a WebDriver.
 */
public class WebDriverUnavailableException extends RuntimeException {
    WebDriverUnavailableException(BrowserTypes browserType, Exception e) {
        super("Failed to establish WebDriver for: " + browserType, e);
    }
}
