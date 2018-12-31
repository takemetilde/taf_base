package com.taf.auto.common;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * An optional Boolean value.
 */
public class Flag {
    private final Optional<Boolean> val;

    public Flag(String value) {
        if(null == value || value.trim().isEmpty()) {
            val = Optional.empty();
        } else {
            val = Optional.of(Boolean.valueOf(value));
        }
    }

    public boolean isPresent() {
        return val.isPresent();
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is
     * null
     */
    public void ifPresent(Consumer<? super Boolean> consumer) {
        val.ifPresent(consumer);
    }

    public Boolean get() {
        return val.get();
    }
}
