package com.taf.auto;

import java.util.regex.Pattern;

import static com.taf.auto.IOUtil.NL;

/**
 * Utility methods for working with {@link String}s.
 *
 * @author AF04261 mmorton
 */
public class StringUtil {
    private StringUtil() { /** static only */ }

    /**
     * Compares each {@link String} line by line. This way newline characters avoid direct comparison.
     *
     * @param a the first
     * @param b the second
     * @return whether first and second contain equivalent lines
     */
    public static boolean allLinesEqual(String a, String b) {
        // considered equal if both are null
        if(a == null && b == null)
            return true;
        // else consider not equal if either is null
        if(a == null || b == null)
            return false;

        String[] aLines = a.split(NL);
        String[] bLines = b.split(NL);

        // shortcut as unequal if different number of lines
        if(aLines.length != bLines.length)
            return false;

        for(int i = 0; i < aLines.length; i++) {
            if(!aLines[i].equals(bLines[i]))
                return false;
        }
        return true;
    }

    /**
     * Splits the given input string by newlines in an OS agnostic way.
     * The regex {@code \r?\n} is used.
     *
     * @param input the potentially multi line input to split
     * @return the input split by newline
     */
    public static String[] splitNewlines(String input) {
        return input.split("\\r?\\n");
    }

    /**
     * Removes any trailing index from the given input. For example "DEV1" will be converted to "DEV". If a
     * trailing index does not exist, the original string will be returned.
     *
     * @param input the input to strip
     * @return the input with the trailing index stripped
     */
    public static String stripTrailingIndex(String input) {
        if(null == input) {
            return null;
        }
        int len = input.length();
        int indexLen = 0;
        for(int i = len; i-->0;) {
            if(Character.isDigit(input.charAt(i))) {
                indexLen++;
            } else {
                break;
            }
        }
        return indexLen == 0 ? input : input.substring(0, len - indexLen);
    }

    /**
     * Replaces all occurrences of {@code toQuote} with {@code ""}. {@code toQuote} is wrapped with with
     * {@link Pattern#quote(String)} to make sure it is treated as a literal and not a regex pattern.
     *
     * @param input the input to strip
     * @param toQuote the literal string to strip out
     * @return the stripped string
     */
    public static String stripQuoted(String input, String toQuote) {
        return input.replaceAll(Pattern.quote(toQuote), "");
    }
}
