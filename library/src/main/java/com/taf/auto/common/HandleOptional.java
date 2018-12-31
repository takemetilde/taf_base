package com.taf.auto.common;


import java.util.Optional;
import java.util.function.Consumer;

/**
 * Extends {@link Handle} to provide convenient support for an {@link Optional} value.
 *
 */
public class HandleOptional<T> extends Handle<Optional<T>> {
    public HandleOptional() {
        this(null);
    }

    public HandleOptional(T initialValue) {
        super(Optional.ofNullable(initialValue));
    }

    public final T pokeOptional(T value) {
        setValue(Optional.of(value));
        return value;
    }

    public final T peekOptional() {
        return getValue().get();
    }

    public final void ifPresent(Consumer<T> consumer) {
        getValue().ifPresent(consumer);
    }

    public final boolean isPresent() {
        return getValue().isPresent();
    }
}
