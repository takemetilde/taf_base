package com.taf.auto.htmlvalidation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Encapsulates a HTML Validation report.
 *
 */
public final class HTMLValidationReport implements ErrorHandler {
    private int warnings = 0;
    private int errors = 0;
    private int fatalErrors = 0;

    private String emitted;

    private final StringBuilder output;

    HTMLValidationReport() {
        output = new StringBuilder("=HTML Validation Report=");
    }
    
    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getFatalErrors() {
        return fatalErrors;
    }

    public void setFatalErrors(int fatalErrors) {
        this.fatalErrors = fatalErrors;
    }

    public boolean hasErrors() {
        return errors + fatalErrors > 0;
    }

    public String getEmitted() {
        return emitted;
    }

    public void setEmitted(String emitted) {
        this.emitted = emitted;
    }

    @Override
    public void warning(SAXParseException saxpe) throws SAXException {
        emitMessage(saxpe, "warning");
    }

    @Override
    public void error(SAXParseException saxpe) throws SAXException {
        emitMessage(saxpe, "error");
    }

    @Override
    public void fatalError(SAXParseException saxpe) throws SAXException {
        emitMessage(saxpe, "fatal error");
    }

    private void emitMessage(SAXParseException e, String messageType)  {
        String e1 = e.getSystemId();
        output.append(e1 == null?"":'\"' + e1 + '\"');
        output.append(":");
        output.append(Integer.toString(e.getLineNumber()));
        output.append(":");
        output.append(Integer.toString(e.getColumnNumber()));
        output.append(": ");
        output.append(messageType);
        output.append(": ");
        output.append(e.getMessage());
        output.append("\n");
    }

    public String toString() {
        return output.toString();
    }
}
