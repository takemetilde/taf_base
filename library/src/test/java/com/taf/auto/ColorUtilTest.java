package com.taf.auto;

import org.junit.Test;

import static com.taf.auto.ColorUtil.extractRGB;
import static com.taf.auto.ColorUtil.hexToRGBA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Unit tests for {@link ColorUtil}.
 *
 */
public class ColorUtilTest {
    @Test
    public void hexToRGBASpectrum() {
        assertEquals("rgba(255, 255, 255, 1)", hexToRGBA("FFFFFF"));

        String errorRed = "rgba(215, 36, 76, 1)";
        // no leading #
        assertEquals(errorRed, hexToRGBA("d7244c"));
        // standard
        assertEquals(errorRed, hexToRGBA("#d7244c"));
        // with alpha specified
        assertEquals(errorRed, hexToRGBA("#d7244cff"));
        // invalid
        assertNotEquals(errorRed, hexToRGBA("#aa244c"));
    }

    @Test(expected = NumberFormatException.class)
    public void hexToRGBAGarbage() {
        hexToRGBA("trash");
    }

    @Test
    public void extractColor() {
        assertEquals("rgb(50, 100, 150)", extractRGB("rgb(50, 100, 150)"));
        assertEquals("rgb(204, 54, 92)", extractRGB("1px solid rgb(204, 54, 92)"));
        assertEquals("rgb(10, 20, 30)", extractRGB("before rgb(10, 20, 30) after"));
    }
}
