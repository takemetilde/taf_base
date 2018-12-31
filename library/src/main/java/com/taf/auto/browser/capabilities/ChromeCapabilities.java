package com.taf.auto.browser.capabilities;

import com.taf.auto.common.Configuration;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Hashtable;
import java.util.Map;

/**
 * Provides capabilities to invoke Chrome.
 *
 */
public final class ChromeCapabilities extends BrowserCapabilities {

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
		chromeOptions.addArguments("test-type", "start-maximized", "no-default-browser-check", "--disable-extensions");
		if (Configuration.IS_PDF_DOWNLOAD_WEBDRIVER) {
			final Map<String, Object> preferences = new Hashtable<String, Object>();
			preferences.put("plugins.always_open_pdf_externally", true);
			preferences.put("download.default_directory", System.getProperty("user.dir"));
			chromeOptions.setExperimentalOption("prefs", preferences);
		}
		mutableCapabilities = chromeOptions;
	}

	private void setDefaultCapabilities() {
		System.setProperty("webdriver.chrome.driver", Configuration.CHROME_WEBDRIVER);
		capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setPlatform(Platform.ANY);
	}

	@Override
	public WebDriver invokeWebDriver() {
		return new ChromeDriver((ChromeOptions) getOptions());
	}

}
