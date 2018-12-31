package com.taf.auto.common;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Library of "pretty printer" methods.
 *
 */
public class PrettyPrinter {

    /** Default delimiter */
    public static final String DELIMITER = ", ";

    public static <K, V> String prettyMap(Map<K, V> map) {
        return prettyMap(map, Optional.empty(), DELIMITER);
    }

    public static <K, V> String prettyMap(Map<K, V> map, Optional<Comparator<K>> optSorter, String delimiter) {
        StringBuilder sb = new StringBuilder();
        Collection<Map.Entry<K, V>> entries;
        if(!optSorter.isPresent()) {
            entries = map.entrySet();
        } else {
            List<K> sortedKeys = new ArrayList<>(map.keySet());
            Collections.sort(sortedKeys, optSorter.get());
            Map<K, V> sortedEntries = new LinkedHashMap<>();
            for(K key : sortedKeys)
                sortedEntries.put(key, map.get(key));
            entries = sortedEntries.entrySet();
        }
        for (Iterator<Map.Entry<K, V>> iter = entries.iterator(); iter.hasNext();) {
            Map.Entry<K, V> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static <E> String prettyCollection(Collection<E> collection) {
        return prettyCollection(collection, DELIMITER);
    }

    public static <E> String prettyCollection(Collection<E> collection, String delimiter) {
        return prettyCollection(collection, Optional.empty(), delimiter);
    }

    /**
     * Concatenates the items in the given Collection into a delimiter separated String. The order
     * of the collection may optionally be sorted. Otherwise items are appended in the order returned
     * by the Collection's iterator.
     *
     * @param collection the items to format
     * @param optSorter the optional sorter
     * @param delimiter the delimiter to use
     * @param <E> the type of elements in collection
     * @return a formatted string containing the items
     */
    public static <E> String prettyCollection(Collection<E> collection, Optional<Comparator<E>> optSorter, String delimiter) {
        StringBuilder sb = new StringBuilder();

        Collection<E> result;
        if(!optSorter.isPresent()) {
            result = collection;
        } else {
            List<E> sorted = new ArrayList<E>(collection);
            Collections.sort(sorted, optSorter.get());
            result = sorted;
        }

        for(Iterator<E> iter = result.iterator(); iter.hasNext();) {
            sb.append(iter.next());
            if(iter.hasNext())
                sb.append(delimiter);
        }
        return sb.toString();
    }

    public static <E> String prettyList(List<E> list) {
        return prettyCollection(list);
    }

    public static <E> String prettyList(List<E> list, String delimiter) {
        return prettyCollection(list, Optional.empty(), delimiter);
    }

    public static <E> String prettyList(List<E> list, Optional<Comparator<E>> optSorter, String delimiter) {
        return prettyCollection(list, optSorter, delimiter);
    }

    public static <A> String prettyArray(A[] array) {
        return prettyArray(array, DELIMITER);
    }

    public static <A> String prettyArray(A[] array, String delimiter) {
        if(null == array) {
            return "";
        }
        return prettyArray(array, 0, array.length, delimiter);
    }

    /**
     * Pretty print the given array.
     *
     * @param array      the array to pretty print
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex   the ending index, exclusive.
     * @param delimiter  the delimiter to place between elements
     * @param <A>        the type of the elements in the array
     * @return a string representing the formatted elements
     */
    public static <A> String prettyArray(A[] array, int beginIndex, int endIndex, String delimiter) {
        if(null == array) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i = beginIndex; i < endIndex; i++) {
            if(i != beginIndex)
                sb.append(delimiter);
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * Scans all the lines to determine the widths of the columns. A column is any characters before the first
     * delimeter or in between.
     *
     * @param lines the lines to scan
     * @param delimiter the delimiter
     * @return the lines with any modifications made
     */
    public static List<String> prettyDelimiters(List<String> lines, String delimiter) {
        List<Integer> widths = new ArrayList<>();

        lines.forEach(recordColumnWidths(widths, delimiter));

        return lines.stream()
                .map(standardizeColumnWidths(widths, delimiter))
                .collect(Collectors.toList());
    }

    static Consumer<String> recordColumnWidths(List<Integer> widths, String delimiter) {
        return line -> {
            String[] columns = line.split(Pattern.quote(delimiter));

            for(int col = 0; col < columns.length; col++) {
                int width = columns[col].length();
                if(col >= widths.size()) {
                    widths.add(Integer.valueOf(width));
                } else {
                    if(width > widths.get(col)) {
                        widths.set(col, width);
                    }
                }
            }
        };
    }

    static Function<String, String> standardizeColumnWidths(List<Integer> widths, String delimiter) {
        return line -> {
            StringBuffer buf = new StringBuffer();
            String[] columns = line.split(Pattern.quote(delimiter));
            for(int col = 0; col < columns.length; col++) {
                buf.append(columns[col]);
                for(int pad = widths.get(col) - columns[col].length(); pad > 0; pad--) {
                    buf.append(' ');
                }
                buf.append(delimiter);
            }
            return buf.toString();
        };
    }

    /**
     * Formats the message of the exception via {@link Throwable#getMessage()}. If the message
     * is null, then name of the exception's class is returned instead.
     *
     * @param t the exception with message to format
     * @return the formatted message.
     */
    public static String prettyExceptionMessage(Throwable t) {
        String message = t.getMessage();
        if(null == message) {
            return t.getClass().getName();
        }
        return String.format("%s: %s", t.getClass().getName(), message);
    }

    /**
     * Outputs the given Exception's cause. Any additional chained causes will be included
     * tabbed in on additional lines.
     *
     * @param t the exception to format
     * @return a represetation of the
     */
    public static String prettyException(Throwable t) {
        Throwable cause = t.getCause();
        if(null == cause || cause == t) {
            return prettyExceptionMessage(t);
        } else {
            StringBuilder msg = new StringBuilder(prettyExceptionMessage(t));
            while(null != cause && cause != cause.getCause()) {
                msg.append("\n  Caused by ").append(prettyExceptionMessage(cause));
                cause = cause.getCause();
            }
            return msg.toString();
        }
    }
}
