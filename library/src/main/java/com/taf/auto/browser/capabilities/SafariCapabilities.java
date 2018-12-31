package com.taf.auto.browser.capabilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

/**
 * Provides capabilities to invoke Safari.
 *
 */
public class SafariCapabilities extends BrowserCapabilities {

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCapability() {
        setDefaultCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOptions() {
        setDefaultOptions();
    }

    private void setDefaultOptions() {
        mutableCapabilities = new SafariOptions();

    }

    private void setDefaultCapabilities() {
        capabilities = DesiredCapabilities.safari();

    }

    @Override
    public WebDriver invokeWebDriver() {
        return new SafariDriver((SafariOptions) getOptions());
    }
}
