package com.taf.auto.jira.app.ui;

import java.util.List;
import java.util.stream.Collectors;

import static com.taf.auto.common.PrettyPrinter.prettyException;
import static com.taf.auto.common.PrettyPrinter.prettyList;

/**
 * A pair of the key of a JIRA Issue and an accompanying exception.
 */
public class FailedKey {
    public final String key;
    public final Throwable exception;

    public FailedKey(String key, Throwable exception) {
        this.key = key;
        this.exception = exception;
    }

    public static List<String> keys(List<FailedKey> keys) {
        return keys.stream().map(e -> e.key).collect(Collectors.toList());
    }

    public static String formatFailedKeys(String description, List<FailedKey> failedKeys) {
        StringBuilder msg = new StringBuilder(description);
        msg.append(": ");
        msg.append(prettyList(keys(failedKeys)));
        failedKeys.forEach(e -> {
            msg.append("\n\n=").append(e.key).append("=\n");
            msg.append(prettyException(e.exception));
        });

        return msg.toString();
    }
}
