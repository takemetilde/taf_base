package com.taf.auto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Library for {@link Stream}.
 */
public class StreamUtil {
    private StreamUtil() { /** static only */ }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

    /**
     * Uses a {@link LinkedHashMap} to provide an ordered map.
     *
     * @param <T> the type of the input elements
     * @param <K> the output type of the key mapping function
     * @param <U> the output type of the value mapping function
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @return a {@code Collector} which collects elements into a {@code LinkedHashMap}
     * whose keys and values are the result of applying mapping functions to
     * the input elements
     */
    public static <T, K, U>
    Collector<T, ?, Map<K,U>> toOrderedMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
    }
}
