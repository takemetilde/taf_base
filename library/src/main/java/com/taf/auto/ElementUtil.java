package com.taf.auto;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

import static com.taf.auto.PageUtil.handledSleep;
import static org.junit.Assert.assertEquals;

/**
 * Created by AF04261 on 5/20/2016.
 */
public class ElementUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ElementUtil.class);

    private ElementUtil() { /** static only */ }

    /**
     * Parses {@link WebElement}'s toString to return the useful locator portion and discarding the rest.
     * @param element the element in question
     * @return describes the locator for the given element
     */
    public static String by(WebElement element) {
        if(null == element)
            return null;
        return webElementByHelper(element.toString());
    }

    /**
     * Extracts the "value" attribute from the given {@link WebElement}.
     * @param element the element in question
     * @return the value for the given element
     */
    public static String peekValue(WebElement element) {
        String value = element.getAttribute("value");
        LOG.trace("Element: " + by(element) + " has value: " + value);
        return value;
    }

    /**
     * Indicates whether the "value" attribute for the given {@link WebElement}
     * is empty or not.
     * @param element the element in question
     * @return <code>ElementUtil.peekValue(element).isEmpty()</code>
     */
    public static boolean isEmpty(WebElement element) {
        return peekValue(element).isEmpty();
    }

    public static void clearThenSendKeys(WebElement element, String keys) {
        element.clear();
        element.sendKeys(keys);
    }

    /**
     * Calls sendKeys one for each byte in the incoming keys.
     *
     * @param element the element in question
     * @param keys the keys to pass to the element one by one
     */
    public static void sendKeysOneByOne(WebElement element, String keys) {
        byte[] bytes = keys.getBytes();
        for (byte b : bytes) {
            element.sendKeys("" + (char) b);
            handledSleep(100);
        }
    }

    static String webElementByHelper(String to) {
        final String token = "->";
        int at = to.indexOf(token);
        if(-1 == at)
            return to;
        to = '[' + to.substring(at + token.length()).trim();
        if(!to.endsWith("]"))
            to += ']';
        return to;
    }

    public static void assertTypeIsPassword(@Nonnull WebElement element) {
        String type = element.getAttribute("type");
        assertEquals("password", type);
    }

    public static By getByFromField(@Nonnull Class<?> clazz, @Nonnull String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);

            if (field.getAnnotation(FindBy.class) == null
                && field.getAnnotation(FindBys.class) == null
                && field.getAnnotation(FindAll.class) == null) {
                throw new RuntimeException("Found field " + fieldName + " in class " + clazz.getName() +  " did not have FindBy, FindBys, or FindAll annotation.");
            }

            return new Annotations(field).buildBy();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find field " + fieldName + " in class " + clazz.getName(), e);
        }
    }

    public static void selectByValue(WebElement select, String value) {
        new Select(select).selectByValue(value);
    }

    /**
     * Returns element.isDisplayed. If the element is not found then it is considered not displayed.
     * @param element the element in question
     * @return whether the element is displayed
     */
    public static boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch(NoSuchElementException nsee) {
            return false;
        }
    }
}
