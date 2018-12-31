package com.taf.auto.common;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Adapts a given {@link Comparator} by taking in different data types and adapting
 * them to what the given Comparator expects.
 *
 */
public final class ComparatorAdapter<T, U> implements Comparator<U> {
    private final Comparator<T> comparator;
    private final Function<U, T> adapter;

    /**
     * Adapts {@link String#CASE_INSENSITIVE_ORDER} and feeds in {@link Object#toString()}.
     */
    public static final Comparator<Object> TOSTRING_CASE_INSENSITIVE_ORDER = new ComparatorAdapter<String, Object>(
            String.CASE_INSENSITIVE_ORDER, o -> o.toString()
    );

    public ComparatorAdapter(Comparator<T> comparator, Function<U, T> adapter) {
        this.comparator = comparator;
        this.adapter = adapter;
    }

    @Override
    public int compare(U o1, U o2) {
        T t1 = adapter.apply(o1);
        T t2 = adapter.apply(o2);
        return comparator.compare(t1, t2);
    }
}
