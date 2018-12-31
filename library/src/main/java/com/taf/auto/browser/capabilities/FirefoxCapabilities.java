package com.taf.auto.browser.capabilities;

import com.taf.auto.common.Configuration;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Provides capabilities to invoke Firefox.
 *
 */
public final class FirefoxCapabilities extends BrowserCapabilities {

	@Override
	public void setCapability() {
		setDefaultCapabilities();
	}

	@Override
	public void setOptions() {
		setDefaultOptions();
	}

	private void setDefaultOptions() {
		mutableCapabilities = new FirefoxOptions(capabilities);
	}

	private void setDefaultCapabilities() {
		System.setProperty("webdriver.gecko.driver", Configuration.FIREFOX_WEBDRIVER);
		capabilities = DesiredCapabilities.firefox();
		capabilities.setBrowserName("firefox");
		capabilities.setPlatform(Platform.ANY);
		if (Configuration.REMOTE) {
			// TODO mmorton remove this once upgraded to latest Firefox
			capabilities.setCapability("marionette", false);
		}

	}

	@Override
	public WebDriver invokeWebDriver() {
		return new FirefoxDriver((FirefoxOptions) getOptions());
	}

}
