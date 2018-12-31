package com.taf.auto;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.taf.auto.WebDriverUtil.driver;

/**
 * Convenience methods for using JavaScript.
 *
 * @author mmorton (AF04261)
 */
public class JSUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JSUtil.class);

    private JSUtil() {
        /**
         * static only
         */
    }

    /**
     * Convenience method that casts {@link WebDriver} to {@link JavascriptExecutor}.
     *
     * @return the WebDriver cast to an executor
     */
    public static JavascriptExecutor getJSExecutor() {
        return (JavascriptExecutor) driver();
    }

    /**
     * Perform a click on the given element.
     *
     * @param element the given element
     */
    public static void click(WebElement element) {
        getJSExecutor().executeScript("arguments[0].click();", element);
    }

    public static void scrollTo(WebElement element) {
        JSUtil.getJSExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public static void setValue(WebElement element, String value) {
        getJSExecutor().executeScript("arguments[0].value ='" + value + "';", element);
    }
    
    public static void setAttribute(WebElement element, String attributeName, String attributeValue){
        getJSExecutor().executeScript("arguments[0].setAttribute('"+attributeName+"', '"+attributeValue+"');", element);
    }

    /**
     * JavaScript constant snippets used by {@link #waitForStableDOM(long)}.
     */
    private interface StableDOM {
        String START = "var target = document.querySelector('body');" +
                "var referenceTime = Date.now();" +
                "window.StableDOM_lastMutation = referenceTime;" +
                "window.StableDOM_observer = new MutationObserver(function(mutations) { " +
                "  console.log('Observed DOM mutation(s)');" +
                "  window.StableDOM_lastMutation = Date.now();" +
                "});" +
                "var config = { attributes: true, childList: true, characterData: true };" +
                "window.StableDOM_observer.observe(target, config);" +
                "return referenceTime;";
        String UPDATE = "return window.StableDOM_lastMutation";
        String STOP = "window.StableDOM_observer.disconnect(); window.StableDOM_observer = null;";
    }

    /**
     * Waits for the DOM to stabilize by attaching a MutationObserver to the body element
     * of the page. Any mutation will update a JS variable with the last mutation time. First
     * a reference time is acquired.
     * Then in a loop, after waiting incrementalWaitInMillis, the last mutation time is accessed
     * and compared to the reference time. If the returned time is equal to the last known
     * time the method will return. Else the reference time is updated to the last mutation time
     * and the loop continues.
     *
     * @param incrementalWaitInMillis how long to wait between polling for the last mutation time
     */
    public static void waitForStableDOM(long incrementalWaitInMillis) {
        JavascriptExecutor executor = getJSExecutor();
        LOG.debug("Beginning wait for stable DOM");
        Long referenceTime = (Long) executor.executeScript(StableDOM.START);
        LOG.debug("Reference time: " + referenceTime);
        while(true) {
            LOG.trace("Sleeping for " + incrementalWaitInMillis + "ms");
            PageUtil.handledSleep(incrementalWaitInMillis);

            Long time = (Long) executor.executeScript(StableDOM.UPDATE);
            LOG.debug("Updated time: " + time);

            // If the DOM has been reloaded, time will be null.  Start over.
            if (time == null) {
                waitForStableDOM(incrementalWaitInMillis);
                return;
            }
            
            if(referenceTime.longValue() == time.longValue()) {
                LOG.debug("DOM has been stable for " + incrementalWaitInMillis + "ms; Continuing...");
                LOG.trace("Disconnecting stable DOM observer");
                executor.executeScript(StableDOM.STOP);
                break;
            }

            referenceTime = time;
        }
    }

    /**
     * waitForChange will wait until timeoutMillis or the DOM to change, whichever comes first.
     * you MUST RUN {@link #startObserver(String, String)} before using this function
     * @param varName is the name that the java script variable we are checking for a boolean value (Same as you used in startObserver)
     *                If you use a unique name here, the risk of clobbering (or being clobbered) is low, so be sure to do so.
     * @param timeoutMillis amount of time to wait before giving up and returning false
     * @return true if the DOM changed on that element or it's sub elements, false otherwise
     */
    public static boolean waitForChange(String varName, int timeoutMillis) {
        boolean changed = false;
        long endTime = System.currentTimeMillis() + timeoutMillis;

        //Wait for the DOM to have changed or timeoutMillis to end
        while(System.currentTimeMillis() < endTime) {
            changed = hasChanged(varName);

            if(changed) {
                LOG.trace("Disconnecting changed DOM observer");
                JavascriptExecutor executor = getJSExecutor();
                executor.executeScript(DOMChangeJS.DISCONNECT(varName));
                break;
            }
            PageUtil.handledSleep(50); //Sleep for 50 milliseconds to avoid excessive http calls
        }
        return changed;
    }

    /**
     * startObserver will start JS watching an element waiting for sub elements in it to update
     * @param xpath is the id of the element you want to watch. WARNING: element must be in the form of an XPATH
     * @param varName is a "unique string" name that the javascript variable will have, save this for later use in "hasChanged"
     *                The purpose of having varNames is to allow multiple observers to run at once in the DOM
     */
    public static void startObserver(String xpath, String varName) {
        JavascriptExecutor executor = getJSExecutor();
        executor.executeScript(DOMChangeJS.OBSERVE(xpath, varName));
    }

    /**
     * stopObserver will stop JS observer on the web element associated with varName
     * @param varName is a "unique string" name that the javascript variable will have, save this for later use in "hasChanged"
     *                The purpose of having varNames is to allow multiple observers to run at once in the DOM
     */
    public static void stopObserver(String varName) {
        JavascriptExecutor executor = getJSExecutor();
        executor.executeScript(DOMChangeJS.DISCONNECT(varName));
    }

    /**
     * hasChanged should be run ONLY AFTER you have ran {@link #startObserver(String, String)}.
     * @param varName is the name that the java script variable we are checking for a boolean value (Same as you used in startObserver)
     *                This should be the same "unique string" that you passed to start observer.
     *                The purpose of having varNames is to allow multiple observers to run at once in the DOM
     * @return true if the variable is true (meaning that the element or subelements from observer has been changed in some way) false otherwise
     */
    public static boolean hasChanged(String varName) {
        JavascriptExecutor executor = getJSExecutor();
        return (boolean) executor.executeScript(DOMChangeJS.UPDATED(varName));
    }

    /**
     * DOMChangeJS contains the javascript code for the above observers
     */
    private static class DOMChangeJS {
        static String OBSERVE(String xpath, String varName) {
            return  "window." + varName + " = false;\n" +
                    "window.observer"+ varName + " = new MutationObserver(function(mutations) {\n" +
                    "    window." + varName + " = true;\n" +
                    "});" +
                    "target = document.evaluate(\"" + xpath + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;\n" +
                    "config = { attributes: true, childList: true, characterData: true,  subtree: true };\n" +
                    "window.observer" + varName + ".observe(target, config);\n";
        }

        static String UPDATED(String varName) {
            return "return window." + varName + ";";
        }

        static String DISCONNECT(String varName) {
            return "window.observer" + varName + ".disconnect();";
        }
    }

    /**
     * Checks to see if the given element (by xpath) is actually visible on screen, and not scrolled off screen.
     * @param xpath the string xpath to the element that will be checked
     * @return true if the element is visible on the portion of the page that is on the users screen, false otherwise
     */
    public static boolean elementInView(String xpath) {
        JavascriptExecutor executor = getJSExecutor();
        executor.executeScript(ElementInViewJS.UTIL);//Add the utility script to the page
        return (boolean) executor.executeScript(ElementInViewJS.INVIEW(xpath));//Execute the Utility script on the given xpath.
    }

    /**
     * ElementInViewJs contains the string representations of javascript used by {@link #elementInView(String)}.
     */
    private static class ElementInViewJS {
        static final String UTIL =
                "function Utils() {}" +
                        "Utils.prototype = {" +
                        "	constructor: Utils," +
                        "	isElementInView: function (element, fullyInView) {" +
                        "		var pageTop = $(window).scrollTop();" +
                        "		var pageBottom = pageTop + $(window).height();" +
                        "		var elementTop = $(element).offset().top;" +
                        "		var elementBottom = elementTop + $(element).height();" +
                        "		if (fullyInView === true) {" +
                        "			return ((pageTop < elementTop) && (pageBottom > elementBottom));" +
                        "		} else {" +
                        "			return ((elementTop <= pageBottom) && (elementBottom >= pageTop));" +
                        "		}" +
                        "	}" +
                        "};" +
                        "window.Utils = new Utils();";

        static String INVIEW(String xpath) {
            return "target = document.evaluate(\"" + xpath + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;\n" +
                    "return window.Utils.isElementInView((target), false);";
        }
    }
}
