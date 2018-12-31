package com.taf.auto.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DataGenUtil}.
 *
 * @author AF04261 mmorton
 */
public class DataGenUtilTest {
    @Test
    public void generateLetters() {
        assertEquals("", DataGenUtil.generateLetters(-1));
        assertEquals("", DataGenUtil.generateLetters(0));
        assertEquals("a", DataGenUtil.generateLetters(1));
        assertEquals("abcdefghijklmnopqrstuvwxyza", DataGenUtil.generateLetters(27));
        assertEquals(50, DataGenUtil.generateLetters(50).length());
    }
}
