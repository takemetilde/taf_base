package com.taf.auto;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.taf.auto.ElementUtil.by;
import static com.taf.auto.JSUtil.getJSExecutor;
import static com.taf.auto.WebDriverUtil.driver;

/**
 * Utility library for a litany of methods dealing with Pages and their usage.
 */
public final class PageUtil {
    private static final Logger LOG = LoggerFactory.getLogger(PageUtil.class);

    private static final long WAIT_MILLIS = 3000;

    private static final long IMPLICIT_WAIT_MILLIS = 5000;

    private static final Dimension IPHONE_6_DIMENSION = new Dimension(375, 559);

    private PageUtil() { /** static only */ }

    /**
     * This method is used to sleep the current thread for any duration of time.
     *
     * @param milliseconds the amount of milliseconds the thread will sleep.
     */
    public static void handledSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ie) {
            LOG.warn("Sleep interrupted", ie);
        }
    }

    public static void clickButton(WebElement webElement) {
        try {
            webElement.click();
        } catch (Exception e) {
            LOG.warn("Failed to click on: " + webElement, e);
        }
    }

    /**
     * This method will wait for the page load to complete or until the timeOut
     * time has elapsed. A negative timeout can cause indefinite page loads.
     *
     * @param timeOut the timeout value in milliseconds.
     */
    public static void waitForPagetoLoad(int timeOut) {
        if (timeOut > 0) {
            driver().manage().timeouts().pageLoadTimeout(timeOut, TimeUnit.SECONDS);
        }
    }

    /**
     * This method returns the text of an element
     *
     * @param elem the element that you wish to retrieve the text from.
     * @return the string of text from the element.
     */
    public static String getTextFromElement(WebElement elem) {
        return elem.getText();
    }

    /**
     * This method is used to select a value in the radio group by its value
     * This method needs to be refactored to use web elements.
     *
     * @param radioGroup the radio group to be selected from.
     * @param ValueToSelect the value in the radio group that will be selected.
     */
    public static void selectRadioButtonByValue(By radioGroup, String ValueToSelect) {
        // Find the radio group element
        List<WebElement> radioLabels = driver().findElements(radioGroup);
        for (int i = 0; i < radioLabels.size(); i++) {
            if (radioLabels.get(i).getText().trim().equalsIgnoreCase(ValueToSelect.trim())) {
                radioLabels.get(i).click();
                break;
            }
        }
    }

    /**
     * This method clicks (Selects) an option passed in the parameter
     * ('itemToSelect'), in the dropdown ('dropDownList') This method needs to
     * be refactored to use web elements.
     *
     * @param dropDownList the dropdown list to be expanded.
     * @param itemToSelect the item in the dropdown list to select.
     */
    public static void selectItemInDropDown(By dropDownList, String itemToSelect) {
        WebElement dropDownElement = driver().findElement(dropDownList);
        List<WebElement> options = dropDownElement.findElements(By.tagName("li"));
        for (WebElement option : options) {
            if (option.getText().toUpperCase().contains(itemToSelect.toUpperCase())) {
                option.click();
                break;
            }
        }

    }

    /**
     * This method can take a string formatted as a CSS selector and return a
     * web element that the selector finds.
     *
     * @param cssSelector the css string that will be searched for on the page.
     * @return the web element that is found.
     */
    public static WebElement findElementByCssSelector(String cssSelector) {
        return driver().findElement(By.cssSelector(cssSelector));
    }

    public static boolean hasElement(By by) {
        return countElements(by) != 0;
    }

//    public static boolean hasElement(By by) {
//        return waitForElement(driver().findElement(by)).isDisplayed();
//    }

    public static boolean hasElement(SearchContext searchContext, By by) {
        return countElements(searchContext, by) != 0;
    }

    public static int countElements(By by) {
        return countElements(driver(), by);
    }

    public static int countElements(SearchContext searchContext, By by) {
        int result = 0;
        long currentWaitMillis = IMPLICIT_WAIT_MILLIS;
        try {
            if (currentWaitMillis > 0) {
                driver().manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
            }

            result = searchContext.findElements(by).size();
        } finally {
            if (currentWaitMillis > 0) {
                driver().manage().timeouts().implicitlyWait(WAIT_MILLIS, TimeUnit.MILLISECONDS);
            }
        }

        return result;
    }

    public static void resizeViewport(String deviceType){
        WebDriver.Window window = driver().manage().window();
        Dimension dimension = window.getSize();
        switch (deviceType.toLowerCase()){
            case "iphone6":
                dimension = IPHONE_6_DIMENSION;
                break;
        }
        window.setSize(dimension);
    }

    /**
     * This method uses WebDriver's getCurrentUrl method to return the URL
     * currently in focus by the WebDriver.
     *
     * @return a string of the current URL.
     */
    public static String getUrl() {
        return driver().getCurrentUrl();
    }

    /**
     * This method returns the title of the current browser.
     *
     * @return the title string.
     */
    public static String getTitle() {
        return driver().getTitle();
    }

    /**
     * This will verify that an element does not exist, use this if you expect no element to be on the screen.
     * This works for elements that don't exist, and elements that are hidden.
     *
     * @param by the given locator
     * @return whether the element specified by the locator does not exist
     */
    public static boolean hasNoElementAsExpected(By by) {
        return WebDriverUtil.runWithTempTimeout(1, TimeUnit.SECONDS, () -> {
            WebElement element;
            try {
                LOG.debug("Waiting for presence validation");
                element = new WebDriverWait(driver(), 1).until(ExpectedConditions.presenceOfElementLocated(by));
            } catch (TimeoutException te) {
                LOG.debug("... timed out");
                return true;
            }

            return Boolean.valueOf(element == null || !element.isDisplayed());
        }).booleanValue();
    }

    /**
     * This method will return the page source as you would see it in a web
     * browser.
     * <p>
     * This can be used to assert specific text exists in the page if the
     * location is not important.
     *
     * @return the page source
     */
    public static String getPageSource() {
        return driver().getPageSource();
    }

    /**
     * Highlights the given element, waits for the given number of milliseconds, and then
     * restores the original border.
     *
     * @param element the given element
     * @param millisecond number of millis to sleep
     */
    public static void highlightElement(WebElement element, int millisecond) {
        Runnable restore = highlightElement(element);
        try {
            handledSleep(millisecond);
        } finally {
            restore.run();
        }
    }

    /**
     * JavaScript snippets used by {@link #highlightElement(WebElement)}.
     */
    private interface HighlightJS {
        String FETCH_AND_SET = "var border = arguments[0].style.border; arguments[0].style.border='3px solid cyan'; return border;";
        String RESTORE = "arguments[0].style.border=arguments[1]";
    }

    /**
     * Highlights the given element by changing its border. Beforehand the
     * original border property is captured. A Runnable is returned that will
     * restore element to the captured border property.
     *
     * @param element the given element
     * @return run this to restore the original border
     */
    public static Runnable highlightElement(WebElement element) {
        JavascriptExecutor exec = getJSExecutor();
        String border = (String) exec.executeScript(HighlightJS.FETCH_AND_SET, element);
        LOG.debug("Extracted border: " + border + " from element: " + by(element));
        return () -> {
            try {
                exec.executeScript(HighlightJS.RESTORE, element, border);
            } catch(Exception e) {
                LOG.warn("Failed to restore border post highlight", e);
            }
        };
    }

    public static void clickElementWithJavascript(WebElement element) {
        if (driver() instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click()",
                    element);
        }
    }

    public static void selectComboItem(WebElement parent, String optionToSelect) {
        WebElement clickableStateDropDown = parent.findElement(By.xpath("//*[@class ='psButton btn btn-primary']"));

        clickableStateDropDown.click();

        List<WebElement> options = parent.findElements(By.tagName("label"));
        for (WebElement option : options) {
            if (option.getText().equals(optionToSelect)) {
                option.click();
                break;
            }
        }
    }

    public static void executeScript(String script) {
        JavascriptExecutor executor = (JavascriptExecutor) driver();
        executor.executeScript(script);

    }

    public static Dimension getWindowDimensions() {
        return driver().manage().window().getSize();
    }

    /**
     * Normally getText will return the text for an element, including all children. This removes the children.
     *
     * @param element the given element
     * @return the text of the given element, ignoring any children text
     */
    public static String getTextIgnoreChildren(WebElement element) {
        String rtn = element.getText();

        for (WebElement child : element.findElements(By.xpath("./*"))) {
            rtn = rtn.replace(child.getText(), "");
        }

        return rtn;
    }

    public static void switchToLastTab() {
        WebDriver webDriver = driver();
        List<String> browserTabs = new ArrayList<>(webDriver.getWindowHandles());
        webDriver.switchTo().window(browserTabs.get(browserTabs.size() - 1));
    }

    public static void closeTab() {
        WebDriver webDriver = driver();
        webDriver.close();
        List<String> browserTabs = new ArrayList<>(webDriver.getWindowHandles());
        webDriver.switchTo().window(browserTabs.get(browserTabs.size() - 1));
    }
}
