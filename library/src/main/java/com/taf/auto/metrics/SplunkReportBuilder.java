package com.taf.auto.metrics;

/**
 * Created by AF04261 on 10/18/2016.
 */
final class SplunkReportBuilder {
    private StringBuilder b;

    SplunkReportBuilder() {
        b = new StringBuilder();
    }

    private void appendKey(String key) {
        if(b.length() != 0)
            b.append(", ");
        b.append(key).append('=');
    }

    SplunkReportBuilder append(String key, String value) {
        return append(key, value, true);
    }

    SplunkReportBuilder append(String key, String value, boolean quoted) {
        appendKey(key);
        if(quoted)
            b.append('\"');
        b.append(value);
        if(quoted)
            b.append('\"');
        return this;
    }

    SplunkReportBuilder append(String key, long value) {
        appendKey(key);
        b.append(value);
        return this;
    }

    SplunkReportBuilder append(String key, boolean value) {
        appendKey(key);
        b.append(value);
        return this;
    }

    public String toString() {
        return b.toString();
    }
}
