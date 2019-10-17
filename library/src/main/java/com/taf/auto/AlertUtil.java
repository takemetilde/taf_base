package com.taf.auto;

import org.openqa.selenium.Alert;

import static com.taf.auto.WebDriverUtil.driver;

/**
 * Convenience methods for working with Alerts.
 *
 */
public final class AlertUtil {
    private AlertUtil() { /** static only */
    }

    public static Alert alert() {
        return driver().switchTo().alert();
    }

    public static void accept() {
        alert().accept();
    }

    public static void dismiss() {
        alert().dismiss();
    }
}
