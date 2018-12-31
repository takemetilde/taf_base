package com.taf.auto.jira.xray;

/**
 * Thrown when a feature file contains invalid syntax.
 */
public class FeatureFileSyntaxException extends Exception {
    public FeatureFileSyntaxException(String message) {
        super(message);
    }
}
