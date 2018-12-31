package com.taf.auto.browser.capabilities;

import com.taf.auto.common.Configuration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides capabilities to invoke Chrome.
 *
 */
public class MobileDeviceCapabilities extends BrowserCapabilities {

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
        final ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setCapability(ChromeOptions.CAPABILITY, capabilities);
        chromeOptions.addArguments("disable-infobars");
        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", Configuration.MOBILE_DEVICE);
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        mutableCapabilities = chromeOptions;
    }

    private void setDefaultCapabilities() {
        System.setProperty("webdriver.chrome.driver", Configuration.CHROME_WEBDRIVER);
        capabilities = DesiredCapabilities.chrome();
    }

    @Override
    public WebDriver invokeWebDriver() {
        return new ChromeDriver((ChromeOptions) getOptions());
    }

}
