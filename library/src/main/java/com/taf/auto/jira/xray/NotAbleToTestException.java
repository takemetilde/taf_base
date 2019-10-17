package com.taf.auto.jira.xray;

/**
 * Special Exception thrown during an Xray Test when the Test Run Status should be NOT_ABLE_TO_TEST instead of the
 * normal FAIL shown when a typical exception is thrown.
 *
 */
public class NotAbleToTestException extends RuntimeException {
    public NotAbleToTestException(String message) {
        super(message);
    }

    public NotAbleToTestException(String message, Throwable cause) {
        super(message, cause);
    }
}
