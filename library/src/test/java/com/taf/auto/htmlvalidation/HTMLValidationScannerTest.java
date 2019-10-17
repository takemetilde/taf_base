package com.taf.auto.htmlvalidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.taf.auto.HTMLUtil.loadURL;
import static com.taf.auto.htmlvalidation.HTMLValidationScanner.validateHtml;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link HTMLValidationScanner}.
 *
 */
public class HTMLValidationScannerTest {
    private static final Logger LOG = LoggerFactory.getLogger(HTMLValidationScannerTest.class);

    //TODO: Offline status needs to be handled here. Temporarily disabled.
    //@Test
    public void validHTML() throws Exception {
        HTMLValidationReport report = validateHtml(loadURL("http://validator.w3.org"));
        LOG.debug("Report:\n" + report.getEmitted());
        assertTrue(report.hasErrors());
    }
}
