package com.taf.auto;

import com.taf.auto.common.Configuration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.taf.auto.PageUtil.highlightElement;
import static com.taf.auto.WebDriverUtil.driver;
import static com.taf.auto.WebDriverUtil.runWithTempTimeout;
import static com.taf.auto.common.PrettyPrinter.prettyArray;
import static java.lang.String.format;

/**
 * Utility library for a variety of wait methods.
 */
public final class PageWaitUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PageWaitUtil.class);

    public static final int WAIT_IN_SECONDS = 20;

    private PageWaitUtil() {
        /**
         * static only
         */
    }

    public static WebElement waitForElement(ExpectedCondition<WebElement> until) {
        return waitForElement(until, WAIT_IN_SECONDS);
    }

    public static <R> R waitForElement(ExpectedCondition<R> until, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver(), timeoutInSeconds);
        wait.ignoring(NoSuchElementException.class);
        wait.ignoring(StaleElementReferenceException.class);
        R result = wait.until(until);
        return result;
    }

    public static WebElement waitForElementVisible(By by) {
        return waitForElement(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitForElementVisible(By by, int timeoutInSeconds) {
        return waitForElement(ExpectedConditions.visibilityOfElementLocated(by), timeoutInSeconds);
    }

    public static WebElement waitForElementVisible(WebElement element) {
        return waitForElement(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement waitForElementVisible(WebElement element, int timeoutInSeconds) {
        return waitForElement(ExpectedConditions.visibilityOf(element), timeoutInSeconds);
    }

    /**
     * Used with Multi methods this bean contains the {@link WebElement} that matched the
     * {@link ExpectedCondition} along with its index from the variable list of elements passsd in.
     */
    public static final class IndexedWebElement {

        public final WebElement element;
        public final int index;

        private IndexedWebElement(WebElement element, int index) {
            this.element = element;
            this.index = index;
        }
    }

    /**
     * Works like {@link #waitForElementVisible(WebElement)} except it takes in a variable list of
     * elements and iterates through them, returning the first one when
     * {@link WebElement#isDisplayed()} is true.
     *
     * @param locators the given locator(s)
     *
     * @return a {@link IndexedWebElement} with the index of the match and the matching element
     * itself or null if timeout
     */
    public static ExpectedCondition<IndexedWebElement> visibilityOfMulti(By... locators) {
        LOG.debug("Considering visibility of: " + prettyArray(locators));
        return driver -> {
            int index = -1;
            // start index at -1 because it's incremented first thing in the loop
            for (By locator : locators) {
                index++;
                WebElement element;
                try {
                    element = driver.findElement(locator);
                    if (element.isDisplayed()) {
                        return new IndexedWebElement(element, index);
                    }
                } catch (NoSuchElementException nsee) {
                    continue;
                } catch (StaleElementReferenceException sere) {
                    continue;
                }
            }
            throw new NoSuchElementException("None of the concerned elements found");
        };
    }

    /**
     * Calls {@link #waitForElementVisibleMulti(int, By...)} with the default timeout of
     * {@link #WAIT_IN_SECONDS}.
     *
     * @param locators the given locator(s)
     * @return the first element found
     */
    public static IndexedWebElement waitForElementVisibleMulti(By... locators) {
        return waitForElementVisibleMulti(WAIT_IN_SECONDS, locators);
    }

    /**
     * Works like {@link #waitForElementVisible(By)} but for a variable list of {@link By} locators.
     * Each pass within the {@link FluentWait} will try each locator in order and return the first
     * one whose {@link WebElement#isDisplayed()} is true.
     * <p>
     * Uses {@link #visibilityOfMulti(By...)} as the {@link ExpectedCondition}.
     *
     * @param timeoutInSeconds the timeout in seconds
     * @param locators the given locator(s)
     * @return the first element found
     */
    public static IndexedWebElement waitForElementVisibleMulti(int timeoutInSeconds, By... locators) {
        return runWithTempTimeout(1, TimeUnit.SECONDS,
                () -> waitForElement(visibilityOfMulti(locators), timeoutInSeconds));
    }

    public static WebElement waitForElementPresent(By by) {
        return waitForElement(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static WebElement waitForElementPresent(By by, int timeoutInSeconds) {
        return waitForElement(ExpectedConditions.presenceOfElementLocated(by), timeoutInSeconds);
    }

    public static WebElement waitForElementClickable(By by) {
        return waitForElement(ExpectedConditions.elementToBeClickable(by));
    }

    public static WebElement waitForElementClickableNoError(By by, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver(), timeoutSeconds);
        WebElement element = null;
        try {
            wait.ignoring(NoSuchElementException.class);
            wait.ignoring(StaleElementReferenceException.class);
            wait.until(ExpectedConditions.elementToBeClickable(by));
            element = driver().findElement(by);
            if (Configuration.DEMO) {
                highlightElement(element, 2000);
            }
        } catch (Exception e) {
            //do nothing when not found
        }
        return element;
    }

    public static void waitForElementNotAnimated(final By by) {
        new FluentWait<WebDriver>(driver()).withTimeout(15, TimeUnit.SECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                //String js = "return $(\":animated\").length";
                //String js = "return $(arguments[0]).is(':animated')";
                //String returnValue = ((JavascriptExecutor) driver()).
                //executeScript(js).toString();
                //return returnValue.equalsIgnoreCase("0") ? true:false;
                WebElement e = driver().findElement(by);
                String holder = e.getAttribute("ng-class");
                String animateHolder = e.getAttribute("animate");
                String animate3Holder = e.getAttribute("ng-style");
                String animate4Holder = e.getAttribute("style");
                return false;
            }

        });
    }

    public static WebElement waitForElementClickable(WebElement element) {
        return waitForElement(ExpectedConditions.elementToBeClickable(element));
    }

    public static WebElement waitForElementGone(WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver(), 15);
        wait.until(ExpectedConditions.stalenessOf(element));
        return element;
    }

    public static void waitForElementGone(By element, int timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver(), timeOutInSeconds);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(element));
    }

    public static void waitForElementNotStale(final By by) {
        new FluentWait<>(driver()).withTimeout(15, TimeUnit.SECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS).until((WebDriver d) -> {
            try {
                d.findElement(by);
            } catch (StaleElementReferenceException e) {
                return false;
            }
            return true;
        });
    }

    public static void waitForElements(final By by, int timeoutSeconds) {
        new FluentWait<>(driver()).withTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS).until((WebDriver t) -> {
            try {
                return !t.findElements(by).isEmpty();
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        });
    }

    public static boolean waitForElementsNoError(final By by, int timeoutSeconds) {
        try {
            new FluentWait<>(driver()).withTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .pollingEvery(500, TimeUnit.MILLISECONDS).until((WebDriver d) -> {
                try {
                    return !d.findElements(by).isEmpty();
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean waitForFindToFail(final By by, int timeoutSeconds) {
        try {
            new FluentWait<>(driver()).withTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .pollingEvery(200, TimeUnit.MILLISECONDS).until((WebDriver d) -> {
                try {
                    d.findElement(by);
                } catch (Exception e) {
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void setImplicitWait(int waitInSseconds) {
        driver().manage().timeouts().implicitlyWait(waitInSseconds, TimeUnit.SECONDS);
    }

    /**
     * This is an efficient version of wait that allows the code to perform waits in tiers. It
     * attempts the action after every provided wait time until action returns true, or all of the
     * waits are run. It will only run as many waits as are required for action to return true.
     * <p>
     * Example Usage:      <code>
     * public boolean isExampleElementDisplayed() throws Exception {
     * return waitTiered(new Callable&lt;Boolean&gt;() {
     *
     * &#064;Override public Boolean call() throws Exception {
     * return hasElement(exampleElementBy);
     * }
     * }, 100, 500, 1000, 2800);
     * }
     * </code>
     * <p>
     * This looks ugly at first glance, but intellij will autogenerate most of it when you start
     * typing new Callable. I'm not sure if eclipse autogenerates, it didn't seem to from my initial
     * look. +1 for IntelliJ This will wait 100 milliseconds, then execute the method named call().
     * If that returns true, waitTiered will return true. If not, waitTiered will repeat for 500,
     * then 1000, then 2800 milliseconds.
     * <p>
     * Primary Uses/BenefitsPage: There are some situations (such as waiting for animations) where
     * we don't know how long it will take and we don't have any events to work with that tell us
     * when the action is completed. One solution is to add a wait for a specific number of seconds.
     * Let's say it will normally take between 100 and 5000 milliseconds for the action we are
     * waiting for to finish. With one wait, we would need to wait 5000 milliseconds every time to
     * ensure that the action has completed.
     * <p>
     * With waitTiered, we can break that 5 seconds into smaller waits to return faster when the
     * website is running faster and still wait longer if the website requires. This is far more
     * efficient then just waiting. Using the example above, let's say that it takes 200
     * milliseconds to run action. Best Case (Success): 100 ms wait + 200 ms action = 300 ms
     * <p>
     * Worst Case (Success or Failure): 100 ms wait + 200 ms action + 500 ms wait + 200 ms action +
     * 1000 ms wait + 200 ms action + 2800 ms wait + 200 ms action = 5200 ms
     * <p>
     * Single Wait Best and Worst Case 5000 ms wait + 200 ms action = 5200 ms
     * <p>
     * If you set up the wait times correctly it can run in the same amount of time as a normal wait
     * in the worst case, and better in every other case.
     *
     * @param action - An implementation of the Callable interface that must return a Boolean.
     * Anonymous functions work well for this.
     * @param waitTimesInMilliseconds - This is 1 to many wait times. If you don't provide a value
     * this method will throw an exception. The waits will be performed in the order they are passed
     * in here. For efficiency I recommend you provide the waits in ascending order.
     * @return - True if action successfully runs after any of the waits, False otherwise.
     * @throws Exception - If no wait times are provided, the wait fails, or any exception raised by
     * action.
     */
    public static boolean waitTiered(Callable<Boolean> action, long... waitTimesInMilliseconds) throws Exception {
        Boolean results = false;

        if (waitTimesInMilliseconds.length == 0) {
            throw new IllegalArgumentException("You must provide at least one wait time");
        }

        for (long waitTime : waitTimesInMilliseconds) {
            Thread.sleep(waitTime);

            if (results = action.call()) {
                break;
            }
        }

        return results;
    }

    /**
     * Waits for an Alert window and returns it.
     *
     * @return the alert
     */
    public static Alert waitForAlert() {
        WebDriverWait wait = new WebDriverWait(driver(), WAIT_IN_SECONDS);
        wait.ignoring(NoAlertPresentException.class);
        Alert alert = wait.until((WebDriver driver) -> AlertUtil.alert());
        return alert;
    }

    public static void waitForSecondTabToLoad() {
        WebDriverWait wait = new WebDriverWait(driver(), 5);
        wait.until((@Nullable WebDriver webDriver) -> webDriver.getWindowHandles().size() == 2);
    }

    /**
     * Immediately calls {@link WebDriver#getCurrentUrl()} and then does a fluent wait for the URL
     * to change value.
     *
     * @param waitInSseconds the number of seconds to wait before timing out
     */
    public static void waitForURLToChange(int waitInSseconds) {
        waitForURLToChange(driver(), waitInSseconds);
    }

    /**
     * Immediately calls {@link WebDriver#getCurrentUrl()} and then does a fluent wait for the URL
     * to change value.
     *
     * @param driver the web driver to use
     * @param waitInSseconds the number of seconds to wait before timing out
     */
    public static void waitForURLToChange(WebDriver driver, int waitInSseconds) {
        String originalUrl = driver.getCurrentUrl();
        // wrap the String in an array to allow lambda to mutate the value
        String[] currentUrlHandle = new String[1];
        LOG.debug("Waiting for URL to change from originalUrl: " + originalUrl);
        ExpectedCondition<Boolean> c = d -> {
            currentUrlHandle[0] = driver.getCurrentUrl();
            LOG.trace("currentUrl: " + currentUrlHandle[0]);

            String pageSource = driver.getPageSource();
            if (pageSource.isEmpty()) {
                LOG.info("Empty page, ignoring: " + pageSource);
                return false;
            }

            if (null == originalUrl) {
                return null != currentUrlHandle[0];
            }
            return !currentUrlHandle[0].equals(originalUrl);
        };
        new WebDriverWait(driver, waitInSseconds).until(c);
        LOG.info(format("Waited until URL changed from: %s to: %s", originalUrl, currentUrlHandle[0]));
    }

    /**
     * Calls {@link #waitForURLToChange(int)} with a timeout of {@link #WAIT_IN_SECONDS}.
     */
    public static void waitForURLToChange() {
        waitForURLToChange(driver());
    }

    /**
     * Calls {@link #waitForURLToChange(int)} with a timeout of {@link #WAIT_IN_SECONDS}.
     *
     * @param driver explicit driver to use if the default {@link WebDriverUtil#driver()} is not
     * desired
     */
    public static void waitForURLToChange(WebDriver driver) {
        waitForURLToChange(driver, WAIT_IN_SECONDS);
    }
}
