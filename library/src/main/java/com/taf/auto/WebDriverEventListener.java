package com.taf.auto;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverEventListener extends AbstractWebDriverEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebDriverEventListener.class);

    @Override

    public void beforeNavigateTo(String url, WebDriver driver) {
        LOG.debug("Navigating to the URL: " + url);
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        LOG.debug("Navigated to:'" + url + "'");
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        LOG.debug("Trying to click on: " + element.toString());
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        LOG.debug("Clicked on: " + element.toString());
    }

    @Override
    public void onException(Throwable error, WebDriver driver) {
        LOG.debug("Error occurred: " + error);
    }
}
