package com.taf.auto.data;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link DataValidationUtil}.
 *
 * @author AF04261 mmorton
 */
public class DataValidationUtilTest {
    @Test
    public void isLetter() {
        assertTrue(DataValidationUtil.isLetter("abc"));
        assertFalse(DataValidationUtil.isLetter(null));
        assertFalse(DataValidationUtil.isLetter(""));
        assertFalse(DataValidationUtil.isLetter("a2c"));
        assertFalse(DataValidationUtil.isLetter("@#%"));
    }
}
