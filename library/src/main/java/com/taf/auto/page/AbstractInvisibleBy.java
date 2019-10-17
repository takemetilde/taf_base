package com.taf.auto.page;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Abstract locator provides the framework for finding the interesting
 * elements to filter against.
 *
 */
public abstract class AbstractInvisibleBy extends By implements InvisibleLocator {
    @Override
    public final List<WebElement> findElements(SearchContext context) {
        return context.findElements(By.tagName("script")).stream()
                .filter(this::filterElement)
                .collect(toList());
    }

    /**
     * Subclass must override to indicate which elements match.
     *
     * @param we the given element
     * @return whether to filter the given element
     */
    protected abstract boolean filterElement(WebElement we);
}
