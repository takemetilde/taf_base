package com.taf.auto;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.function.Supplier;

import static com.taf.auto.WebDriverUtil.driver;

/**
 * Common code for dealing with iFrames.
 *
 * @author mmorton AF04261
 */
public final class FrameUtil {

    private FrameUtil() { /** static only */ }

    public static void switchToDefaultContent() {
        driver().switchTo().defaultContent();
    }

    public static void switchTo(Supplier<By> frameLocator) {
        if(null == frameLocator)
            throw new NullPointerException("null frame not allowed. To switch to default content call switchToDefaultContent() instead");

        WebDriver.TargetLocator locator = driver().switchTo();
        locator.defaultContent();

        By by = frameLocator.get();

        /**
         * Using locator.frame(WebElement frameElement) INSTEAD of locator.frame(String nameOrId)
         * as it tends to run orders of magnitude faster.
         */
        WebElement frameElem = driver().findElement(by);
        locator.frame(frameElem);
    }

    /**
     * Switches to the frame specified by handle, executes an action on that frame,
     * then switches back to the main content frame.
     *
     * @param handle The FrameHandle for the frame in which the action will be executed
     * @param action The action to executeInFrame
     */
    public static void executeInFrame(Supplier<By> handle, Runnable action) {
        switchTo(handle);
        action.run();
    }
}
