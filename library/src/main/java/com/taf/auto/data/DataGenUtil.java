package com.taf.auto.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for creating various kinds of test data.
 *
 */
public final class DataGenUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DataGenUtil.class);

    private DataGenUtil() {
        /** static only */
    }

    /**
     * Generates a string of the given length by repeating the characters a-z as many times as necessary.
     *
     * @param length the length of the desired string
     * @return the desired sequence of repeated letters
     */
    public static String generateLetters(int length) {
        StringBuilder gen = new StringBuilder();
        for(int i = 0; i < length; i++) {
            char c = (char) (i % 26 + 'a');
            gen.append(c);
        }
        String str = gen.toString();
        LOG.debug("Generated alpha string of length " + length + ": " + str);
        return str;
    }
}
