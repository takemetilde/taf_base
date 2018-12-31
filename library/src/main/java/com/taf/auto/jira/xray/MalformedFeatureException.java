package com.taf.auto.jira.xray;

/**
 * Exception thrown when a feature file is malformed.
 *
 * @author AF04261 mmorton
 */
public class MalformedFeatureException extends RuntimeException {
    public MalformedFeatureException(String message) {
        super(message);
    }
}
