package com.taf.auto.common;

/**
 * The property keys used by {@link Configuration}.
 */
public interface ConfigurationKeys {
    String EXECUTION_ENVIRONMENT = "execution.environment";

    String EXECUTION_ID = prefix("executionid");
    String BRAND = prefix("brand");
    String SUPPRESS_SCREENSHOT = prefix("suppress.screenshot");
    String SPLUNK_FILE = prefix("splunk.file");

    String BROWSER = "selenium.browser";

    static String prefix(String key) { return "anthem.auto." + key; }
}
