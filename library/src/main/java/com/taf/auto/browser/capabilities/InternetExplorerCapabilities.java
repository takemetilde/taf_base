package com.taf.auto.browser.capabilities;

import com.taf.auto.common.Configuration;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Provides capabilities to invoke Internet explorer.
 *
 */
public final class InternetExplorerCapabilities extends BrowserCapabilities {

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
        mutableCapabilities = new InternetExplorerOptions(capabilities);

    }

    private void setDefaultCapabilities() {
        System.setProperty("webdriver.ie.driver", Configuration.IE_WEBDRIVER);
        capabilities = DesiredCapabilities.internetExplorer();
        capabilities.setPlatform(Platform.WINDOWS);
        capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        capabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "about:blank");
        capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
        capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
        capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    }

    @Override
    public WebDriver invokeWebDriver() {
        return new InternetExplorerDriver((InternetExplorerOptions) getOptions());
    }
}
