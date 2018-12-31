package com.taf.auto.spellcheck;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SpellingScanner}.
 *
 * @author AF04261 mmorton
 */
public class SpellingScannerTest {
    @Test
    public void rudimentary() throws IOException {
        final String validWord = "spelled";
        assertFalse(SpellingScanner.hasMatches(validWord));
        assertFalse(SpellingScanner.check(validWord).hasEntries());

        final String invalidWord = "mispeled";
        assertTrue(SpellingScanner.hasMatches(invalidWord));
        assertTrue(SpellingScanner.check(invalidWord).hasEntries());
    }
}
