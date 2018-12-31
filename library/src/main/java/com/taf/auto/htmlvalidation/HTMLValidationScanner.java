package com.taf.auto.htmlvalidation;

import com.taf.auto.IOUtil;
import nu.validator.messages.MessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.messages.TextMessageEmitter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.SourceCode;
import nu.validator.validation.SimpleDocumentValidator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static com.taf.auto.WebDriverUtil.driver;

/**
 * Scans the provided HTML or URL and applies W3C HTML validation.
 */
public final class HTMLValidationScanner {
    /**
     * Verifies that a HTML content is valid.
     *
     * @param htmlContent the HTML content
     * @return true if it is valid, false otherwise
     */
    public static HTMLValidationReport validateHtml(String htmlContent) {

        byte[] bytes;
        try {
            bytes = htmlContent.getBytes("UTF-8");
        } catch(UnsupportedEncodingException uee) {
            throw new RuntimeException("Unable to access HTML as UTF-8", uee);
        }

        HTMLValidationReport report = new HTMLValidationReport();

        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new ByteArrayInputStream(bytes);
            out = new ByteArrayOutputStream();

            SourceCode sourceCode = new SourceCode();
            ImageCollector imageCollector = new ImageCollector(sourceCode);
            boolean showSource = false;
            MessageEmitter emitter = new TextMessageEmitter(out, false);
            MessageEmitterAdapter errorHandler = new MessageEmitterAdapter(sourceCode, showSource, imageCollector, 0, false, emitter);
            errorHandler.setErrorsOnly(true);


            SimpleDocumentValidator validator = new SimpleDocumentValidator();
            try {
                validator.setUpMainSchema("http://s.validator.nu/html5-rdfalite.rnc", report);
                validator.setUpValidatorAndParsers(errorHandler, true, false);
                validator.checkHtmlInputSource(new InputSource(in));

                // this must be called to flush the output stream
                emitter.endMessages();
            } catch (Exception e) {
                throw new RuntimeException("Unable to validate", e);
            }

            report.setWarnings(errorHandler.getWarnings());
            report.setErrors(errorHandler.getErrors());
            report.setFatalErrors(errorHandler.getFatalErrors());

            report.setEmitted(new String(out.toByteArray()));
        } finally {
            IOUtil.safeClose(in);
            IOUtil.safeClose(out);
        }

        return report;
    }

    public static HTMLValidationReport validateHtml(WebElement element) {
        String html = element.getAttribute("outerHTML");
        return validateHtml(html);
    }

    /**
     * Validates using the {@link WebElement} with the body tag.
     *
     * @return the validation report
     */
    public static HTMLValidationReport validateHtml() {
        WebElement body = driver().findElement(By.tagName("body"));
        return validateHtml(body);
    }
}
