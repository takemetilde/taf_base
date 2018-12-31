package com.taf.auto.cucumber.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for a Cucumber test result.
 *
 */
public class CucumberTestResult extends AbstractCucumberResult {
    @JsonProperty
    public CucumberTestResultElement[] elements;

    @JsonProperty
    public String uri;

    @JsonProperty
    public CucumberTestResultTag[] tags;

    public void appendElement(CucumberTestResultElement[] elementsToAppend) {
        CucumberTestResultElement[] moar = new CucumberTestResultElement[elements.length + elementsToAppend.length];
        System.arraycopy(elements, 0, moar, 0, elements.length);;
        System.arraycopy(elementsToAppend, 0, moar, elements.length, elementsToAppend.length);
        elements = moar;
    }
}
