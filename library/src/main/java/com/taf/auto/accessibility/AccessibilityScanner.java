package com.taf.auto.accessibility;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

import static com.taf.auto.WebDriverUtil.driver;
import static com.taf.auto.accessibility.AccessibilityAuditReport.Keys.*;

/**
 * The chassis for running an accessibility audit.
 */
public final class AccessibilityScanner {
    private static final Logger LOG = LoggerFactory.getLogger(AccessibilityScanner.class);

    private final JavascriptExecutor js;
    private final JSAccessibilityFactory jsAccessibilityFactory;
    private final Optional<String> parentSelector;

    private final List<AccessibilityResult> errors;
    private final List<AccessibilityResult> warnings;

    private String report;

    /**
     * Constructs a scanner that will scan from the root of the DOM.
     *
     * @throws IOException if the {@link JSAccessibilityFactory} fails to load
     */
    public AccessibilityScanner() throws IOException {
        this(Optional.empty());
    }

    /**
     * Constructs a scanner that will scan from the given parent selector. For an id such as "foo", pass in "#foo".
     * Or to select a class such as "bar", pass in ".bar".
     * For more information on how to format CSS Selectors, visit http://www.w3schools.com/cssref/css_selectors.asp.
     *
     * @param parentSelector the given selector for the parent
     * @throws IOException if the {@link JSAccessibilityFactory} fails to load
     */
    public AccessibilityScanner(@Nonnull String parentSelector) throws IOException {
        this(Optional.of(parentSelector));
    }

    /**
     * Constructs a scanner that will scan from the root of the DOM if the parentSelector is empty. Otherwise it will
     * scan the specified parent and its children.
     *
     * @param parentSelector the optional selector to use as the parent
     *
     * @throws IOException if the {@link JSAccessibilityFactory} fails to load
     */
    public AccessibilityScanner(@Nonnull Optional<String> parentSelector) throws IOException {
        this.parentSelector = resolveSelector(parentSelector);
        js = (JavascriptExecutor) driver();
        jsAccessibilityFactory = JSAccessibilityFactory.getInstance();
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    private static Optional<String> resolveSelector(Optional<String> parentSelector) {
        if(parentSelector.isPresent() && parentSelector.get().isEmpty()) {
            LOG.debug("Empty selector converted to absent selector");
            return Optional.empty();
        }
        return parentSelector;
    }

    /**
     * Entry point that runs the audit. The results of the audit added to {@link AccessibilityAuditReport}.
     */
    public void runAccessibilityAudit() {
        runJsAccessibilityAudit();
        checkInputs();
        generateReport();
    }

    private void runJsAccessibilityAudit() {
        LOG.info("Running accessibility audit");

        js.executeScript(jsAccessibilityFactory.getAccessibilityContent());

        StringBuilder buf = new StringBuilder("var auditConfig = new axs.AuditConfiguration();");
        parentSelector.ifPresent(selector -> {
            LOG.debug("Injecting selector: " + selector);
            buf.append("\nauditConfig.scope = document.querySelector('").append(selector).append("');");
        });

        buf.append("\n\n")
            .append("var results = axs.Audit.run(auditConfig);\n")
            .append("var auditResults = axs.Audit.auditResults(results);\n")
            .append("var report = axs.Audit.createReport(results);\nreturn report;");

        String accessibilityTests = buf.toString();
        LOG.debug("Executing script:\n" + accessibilityTests);
        report = (String) js.executeScript(accessibilityTests);

        LOG.info("Report:\n" + report);

        try {
            LOG.info((String)js.executeScript("$.active;"));
        } catch (WebDriverException wde) {
            LOG.info("++++++++Injecting jQuery+++++++++++++");
            js.executeScript(jsAccessibilityFactory.getJqueryContent());
        }
    }

    private void decorateElements(List<AccessibilityResult> results, String color) {
        for (AccessibilityResult result : results) {
            List<String> locators = result.getElements();
            addBorder(locators, result.getRule(), color);
        }
    }

    private static List<AccessibilityResult> parseReport(String report, String filterOn) {
        if (report == null)
            throw new NullPointerException("Report to parse cannot be null");

        String filterLowerCase = filterOn.toLowerCase();
        if (filterLowerCase.contains("error"))
            filterOn = "Error:";
        else if (filterLowerCase.contains("warning"))
            filterOn = "Warning:";
        else
            throw new IllegalArgumentException("Currently only support filtering on Error: and Warning:");

        List<AccessibilityResult> parsedResult = new ArrayList<>();
        int startError = report.indexOf(filterOn);
        while (startError > 0) {
            AccessibilityResult accessibilityResult = new AccessibilityResult();
            int end = report.indexOf("\n\n", startError);
            String error = report.substring(startError + filterOn.length(), end).trim();
            accessibilityResult.setRule(error.substring(0, error.indexOf("\n")));
            String link = null;
            String[] locators;
            int elementStart = error.indexOf("\n") + 1;
            String element;
            if (error.indexOf("See") > 0) {
                element = error.substring(elementStart, error.indexOf("See"));
                link = error.substring(error.indexOf("See"));
            } else {
                element = error.substring(elementStart);
            }
            locators = element.split("\n");
            accessibilityResult.setElements(Arrays.asList(locators));
            accessibilityResult.setUrl(link);
            parsedResult.add(accessibilityResult);
            startError = report.indexOf(filterOn, end);
        }
        return parsedResult;
    }

    /**
     * ALT TEXT - INPUT IMAGE <INPUT TYPE = 'image' ...>
     * 	- Image input elements should have alt attributes
     * 	- Image input elements should not have alt attributes empty
     */
    private void checkInputs(){
        // TODO use the locator passed into the constructors. Currently scans from root
        LOG.info("Checking inputs");
        List<WebElement> textInputs = driver().findElements(By.xpath("//input"));
        List<WebElement> failedElems = new ArrayList<>();
        for (WebElement textInput : textInputs) {
            String ariaDescribedByText = textInput.getAttribute("aria-describedby");
            ariaDescribedByText = ariaDescribedByText == null ? "" : ariaDescribedByText;
            if(ariaDescribedByText.isEmpty()){
                failedElems.add(textInput);
            }
        }

        if (!failedElems.isEmpty()){
            addToReport("\nError",  "aria-describedby is missing on the following elements", failedElems);
        }
    }

    private void addBorder(List<String> locators, String rule, String color) {
        for (String locator : locators) {
            if(".".equals(locator)) {
                LOG.info("Skipping adding border for: " + locator);
                continue;
            }
            LOG.debug("Adding border for: " + locator);
            rule = "<p>" + rule + "</p>";
            String script = "$(\"" + locator + "\").css(\"border\",\"5px solid " + color + "\")";
            try{
                js.executeScript(script);
            }
            catch(Exception e){
                LOG.warn("Unable to execute script: " + script, e);
            }
        }
    }

    private void addToReport(String type, String rule, List<WebElement> elems){
        int at = report.indexOf("\n\n*** End accessibility audit results ***");
        String extendedReport = at != -1 ? report.substring(0, at) : "";

        extendedReport += type + ": " + rule + "\n";

        for (WebElement elem : elems) {
            extendedReport += getLocator(elem) + "\n";
        }

        report = extendedReport + "\n\n*** End accessibility audit results ***";
    }

    private static String getLocator(WebElement element) {
        if (!element.getAttribute("id").isEmpty())
            return "#" + element.getAttribute("id");
        String classLocator = "";
        String[] classes = element.getAttribute("class").split(" ");
        for (String className : classes){
            classLocator += "." + className;
        }

        if (!classLocator.isEmpty())
            return classLocator;

        return null;
    }

    private void generateReport(){
        LOG.info("Generating report");
        errors.clear();
        errors.addAll(parseReport(report, "Error:"));

        warnings.clear();
        warnings.addAll(parseReport(report, "Warning:"));

        decorateElements(errors, "red");
        decorateElements(warnings, "yellow");

        Map<String, Object> reportMap = AccessibilityAuditReport.getReport();

        reportMap.put(ERROR, errors);
        reportMap.put(WARNING, warnings);
        reportMap.put(PLAIN_REPORT, report);
    }
}
