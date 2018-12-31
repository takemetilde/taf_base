package com.taf.auto.browser.capabilities;

import com.taf.auto.common.BrowserTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrowserCapabilityFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserCapabilityFactory.class);

    private BrowserCapabilityFactory() {
        // private
    }

    /**
     * Provides browser capabilities as per browser type.
     *
     * @param browserType
     * @return BrowserCapabilities
     */
    public static BrowserCapabilities getBrowserCapabilities(final BrowserTypes browserType) {
        try {
            switch (browserType) {
                case IE:
                    return getBrowserCapabilities(browserType, InternetExplorerCapabilities.class);
                case FIREFOX:
                    return getBrowserCapabilities(browserType, FirefoxCapabilities.class);
                case CHROME:
                    return getBrowserCapabilities(browserType, ChromeCapabilities.class);
                case SAFARI:
                    return getBrowserCapabilities(browserType, SafariCapabilities.class);
                case MOBILE_DEVICE:
                    LOG.info("Getting mobile device capabilities");
                    return getBrowserCapabilities(browserType, MobileDeviceCapabilities.class);
                case MOBILE_CONFIG:
                    LOG.info("Getting custom mobile device capabilities");
                    return getBrowserCapabilities(browserType, MobileCustomDeviceCapabilities.class);
                default:
                    throw new RuntimeException("Browser type not supported: " + browserType);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides browser capabilities with browser type and custom capability class.
     *
     * @param browserType
     * @param clazz
     * @return BrowserCapabilities
     */
    public static BrowserCapabilities getBrowserCapabilities(final BrowserTypes browserType,
            final Class<? extends BrowserCapabilities> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create an instance of the class");
        }
    }

    /**
     * Provides browser capabilities with browser type and custom capability class name.
     *
     * @param browserType
     * @param customCapabilityClassName
     * @return BrowserCapabilities
     */
    public static BrowserCapabilities getBrowserCapabilities(final BrowserTypes browserType,
            final String customCapabilityClassName) {
        try {
            final Class<?> clazz = Class.forName(customCapabilityClassName);
            return (BrowserCapabilities) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to create an instance of the class");
        }
    }
}
