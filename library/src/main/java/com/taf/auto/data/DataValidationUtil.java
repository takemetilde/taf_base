package com.taf.auto.data;

/**
 * Utility methods for testing data validation.
 *
 */
public final class DataValidationUtil {
    private DataValidationUtil() {
        /** static only */
    }

    /**
     * Scans the given string to determine if it contains only letters of the alphabet.
     *
     * @param str the string to analyze
     * @return whether the string has only letters.
     */
    public static boolean isLetter(String str) {
        if(null == str || str.isEmpty())
            return false;
        for(int i = 0, len = str.length(); i < len; i++ ) {
            if(!Character.isLetter(str.charAt(i)))
                return false;
        }
        return true;
    }
}
