package com.taf.auto.spellcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the results of running a spelling check with the {@link SpellingScanner}.
 *
 * @author AF04261 mmorton
 */
public final class SpellingReport {
    private final List<String> entries;

    SpellingReport() {
        entries = new ArrayList<>();
    }

    void addEntry(String entry) {
        entries.add(entry);
    }

    /**
     * Gets the entries as an unmodifiable map. Call {@link #addEntry(String)} to introduce an entry to the report.
     *
     * @return unmodifiable list of the entries
     */
    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Indicates if the report contains any entries. A report with zero entries suggests that the input text that was
     * scanned does not contain any spelling errors.
     *
     * @return whether the report contains any entries.
     */
    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    public String toString() {
        StringBuilder msg = new StringBuilder("Spelling Report:");
        if(hasEntries()) {
            for (String entry : entries) {
                msg.append('\n').append(entry);
            }
        } else {
            msg.append(" no errors found.");
        }
        return msg.toString();
    }
}
