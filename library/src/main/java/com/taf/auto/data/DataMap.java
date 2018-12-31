package com.taf.auto.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a map's key/value functionality while providing strong typing via {@link DataKey}.
 *
 * @see DataKey
 */
public final class DataMap {
    private final Map<DataKey<?>, Object> map;

    public DataMap() {
        map = new HashMap<>();
    }

    /**
     * Puts the given value into the map with the given key.
     *
     * @param key the key to store with
     * @param value the value to store, may not be {@code null}
     * @param <T> the type of the value
     *
     * @throws IllegalArgumentException if the value is {@code null}
     */
    public <T> void poke(DataKey<T> key, T value) {
        if(null == value) {
            throw new IllegalArgumentException("null is not an allowed value");
        }
        map.put(key, value);
    }

    /**
     * Gets a typed value from the map with the given key.
     *
     * @param key the key to retrieve with
     * @param <T> the type of the value
     * @return the value
     *
     * @throws IndexOutOfBoundsException if the map does not contain a value for the key
     */
    public <T> T peek(DataKey<T> key) {
        Object value = map.get(key);
        if(null == value)
            throw new IndexOutOfBoundsException("Value never put for key: " + key);
        try {
            T castValue = (T) value;
            return castValue;
        } catch (ClassCastException cce) {
            /** This should never happen because {@link #poke(DataKey, Object)} will restrict the type */
            throw new RuntimeException("Failed to cast value: " + value, cce);
        }
    }
}
