package com.taf.auto.common;

/**
 * Convenient way to pass a final reference to an object to use elsewhere.
 * Typically the Handle is passed into a lambda expression and retrieve a value
 * set within.
 *
 */
public class Handle<T> {
    private T value;

    public Handle(T initialValue) {
        value = initialValue;
    }

    public final T getValue() {
        return value;
    }

    public final void setValue(T value) {
        this.value = value;
    }

    public String toString() {
        return null != value ? value.toString() : null;
    }
}
