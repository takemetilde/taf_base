package com.taf.auto.browser.capabilities;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Abstract class for getting various browser capabilities.
 *
 */
public abstract class BrowserCapabilities {

    protected DesiredCapabilities capabilities;
    protected MutableCapabilities mutableCapabilities;

    public BrowserCapabilities() {
        setCapability();
        setOptions();
    }

    /**
     * Returns desired capabilities.
     *
     * @return DesiredCapabilities
     */
    public DesiredCapabilities getCapability() {
        return capabilities;
    }

    /**
     * Set Desired capabilities.
     *
     */
    public abstract void setCapability();

    /**
     * Returns Mutable capability options.
     *
     * @return
     */
    public MutableCapabilities getOptions() {
        return mutableCapabilities;
    }

    /**
     * Set Mutable capability options
     *
     */
    public abstract void setOptions();

    /**
     * This method invokes instance as per browser capabilities.
     *
     * @return WebDriver
     */
    public abstract WebDriver invokeWebDriver();

}
