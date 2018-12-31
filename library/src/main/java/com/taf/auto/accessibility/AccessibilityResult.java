package com.taf.auto.accessibility;

import java.util.List;

/**
 * POJO that encapsulates the results of an accessiblity test.
 */
public final class AccessibilityResult {
    private String rule;
    private List<String> elements;
    private String url;

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public List<String> getElements() {
        return elements;
    }

    public void setElements(List<String> elements) {
        this.elements = elements;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
