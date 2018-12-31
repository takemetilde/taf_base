package com.taf.auto.accessibility;

import org.junit.Test;

import java.io.IOException;

import static com.taf.auto.accessibility.JSAccessibilityFactory.*;

/**
 * Test cases for {@link JSAccessibilityFactory}
 */
public class JSAccessibilityFactoryTest {
    @Test
    public void locateJQueryScript() throws IOException {
        loadResource(JQUERY_SCRIPT);
    }

    @Test
    public void locateAuditScript() throws IOException {
        loadResource(AUDIT_SCRIPT);
    }
}
