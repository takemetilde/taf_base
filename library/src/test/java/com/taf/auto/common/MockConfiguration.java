package com.taf.auto.common;

import java.util.Properties;
import java.util.function.Consumer;

import static com.taf.auto.common.Configuration.MOCK_ENV;

/**
 * A configuration adapter for mocking required data.
 *
 * @author AF04261 mmorton
 */
public class MockConfiguration implements Consumer<Properties> {

    @Override
    public void accept(Properties prop) {
        prop.setProperty(ConfigurationKeys.EXECUTION_ENVIRONMENT, MOCK_ENV);
        prop.setProperty(ConfigurationKeys.BROWSER, BrowserTypes.CHROME.toString());
    }

    public static void install() {
        ConfigurationPropertyAdapter.overridePropertyAdapter(new MockConfiguration());
    }
}
