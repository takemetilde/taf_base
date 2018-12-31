package com.taf.auto.spellcheck;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides static, thread-safe access to the spell checking library {@link JLanguageTool}. The specifics of
 * the library are encapsulated within this class to avoid tight coupling. Call the {@link #check(String)} method
 * to obtain a {@link SpellingReport} that contains 0+ entries. A report with 0 entries indicates a passed check.
 *
 * @author AF04261 mmorton
 */
public final class SpellingScanner {
    private static final Logger LOG = LoggerFactory.getLogger(SpellingScanner.class);

    private static final ThreadLocalScanners scanners = new ThreadLocalScanners();

    private static final Class<? extends Language> DEFAULT_LANGUAGE = AmericanEnglish.class;

    private SpellingScanner() {
        /** static only */
    }

    public static SpellingReport check(String input) throws IOException {
        JLanguageTool spellChecker = getSpellChecker();
        List<RuleMatch> matches = spellChecker.check(input);
        SpellingReport report = new SpellingReport();
        for(RuleMatch match : matches) {
            report.addEntry(formatMatch(match));
        }
        return report;
    }

    /**
     * Access the tool for spell checking using the {@link #DEFAULT_LANGUAGE}.
     *
     * @return
     */
    private static JLanguageTool getSpellChecker() {
        return getSpellChecker(DEFAULT_LANGUAGE);
    }

    private static JLanguageTool getSpellChecker(Class<? extends Language> languageClazz) {
        Map<Class<? extends Language>, JLanguageTool> toolsForThread = scanners.get();
        JLanguageTool tool = toolsForThread.get(languageClazz);
        if(null == tool) {
            LOG.debug("Instantiating tool for thread: " + Thread.currentThread().getId() + " for language: " + languageClazz);
            Language language;
            try {
                language = languageClazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Language not available: " + languageClazz, e);
            }
            tool = new JLanguageTool(language);
            configure(tool);
            toolsForThread.put(languageClazz, tool);
        }
        return tool;
    }

    /**
     * Additional configuration of the spell checker.
     *
     * @param tool
     */
    private static void configure(JLanguageTool tool) {
        tool.disableRule("UPPERCASE_SENTENCE_START");
    }

    /**
     * Scans the text for rules matches (errors or warnings) using {@link #DEFAULT_LANGUAGE}.
     *
     * @param input the text to scan
     * @return whether the input contains any rules matches.
     * @throws IOException if {@link JLanguageTool#check(String)} throws
     */
    public static boolean hasMatches(String input) throws IOException {
        if(null == input || input.isEmpty())
            return false;
        return hasMatches(input, DEFAULT_LANGUAGE);
    }

    /**
     * Scans the text for rules matches (errors or warnings) using the given language.
     *
     * @param input the text to scan
     * @param languageClazz the class of the language to check against
     * @return whether any active {@link org.languagetool.rules.Rule} have matched the input text
     * @throws IOException if {@link JLanguageTool#check(String)} throws
     */
    private static boolean hasMatches(String input, Class<? extends Language> languageClazz) throws IOException {
        List<RuleMatch> matches = getSpellChecker(languageClazz).check(input);
        if(LOG.isDebugEnabled())
            LOG.debug(formatMatches(matches));
        return !matches.isEmpty();
    }

    /**
     * Scans the text of the given element for spelling issues.
     *
     * @param element the given element
     * @return whether the element has spelling issues
     * @throws IOException if {@link JLanguageTool#check(String)} throws
     */
    public static boolean hasMatches(WebElement element) throws IOException {
        return hasMatches(element.getText());
    }

    private static void formatMatch(RuleMatch match, StringBuilder msg) {
        msg.append("Potential error at line ").append(match.getLine()).append(", column ").append(match.getColumn()).append(": ").append(match.getMessage());
        msg.append("\nSuggested correction: ").append(match.getSuggestedReplacements());
    }

    private static String formatMatch(RuleMatch match) {
        StringBuilder msg = new StringBuilder();
        formatMatch(match, msg);
        return msg.toString();
    }

    static String formatMatches(List<RuleMatch> matches) {
        StringBuilder msg = new StringBuilder();
        boolean first = true;
        for (RuleMatch match : matches) {
            if(first)
                first = false;
            else
                msg.append('\n');
            formatMatch(match, msg);
        }
        return msg.toString();
    }

    private static class ThreadLocalScanners extends ThreadLocal<Map<Class<? extends Language>, JLanguageTool>> {
        @Override
        protected Map<Class<? extends Language>, JLanguageTool> initialValue() {
            return new HashMap<>();
        }
    }
}
