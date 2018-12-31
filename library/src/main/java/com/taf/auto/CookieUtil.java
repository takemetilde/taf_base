package com.taf.auto;

import com.taf.auto.common.PrettyPrinter;
import org.openqa.selenium.Cookie;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static com.taf.auto.WebDriverUtil.driver;

/**
 * Utilities for {@link Cookie}.
 */
public final class CookieUtil {
    private CookieUtil() { /** static only */ }

    public static final class CookieByNameComparator implements Comparator<Cookie> {
        @Override
        public int compare(Cookie o1, Cookie o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static String formatCookies() {
        Set<Cookie> cookies = driver().manage().getCookies();
        return PrettyPrinter.prettyCollection(cookies, Optional.of(new CookieByNameComparator()), "\n");
    }
}
