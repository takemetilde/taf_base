package com.taf.auto.accessibility;

import com.taf.auto.IOUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides access to the raw JavaScript assets used by {@link AccessibilityScanner} to administer an
 * accessibility audit.
 *
 * The scanner leverages the script from https://github.com/GoogleChrome/accessibility-developer-tools.
 * A copy of this script is kept in <tt>resources/accessibility/accessibilityAudit.js</tt> as is loaded
 * by this factory via the classpath at runtime.
 *
 */
public final class JSAccessibilityFactory {
    private static final Logger LOG = LoggerFactory.getLogger(JSAccessibilityFactory.class);

    static final String JQUERY_SCRIPT = "jquery.min.js";
    static final String AUDIT_SCRIPT = "accessibilityAudit.js";

    private static JSAccessibilityFactory instance = null;

    private final String jqueryContent;
    private final String accessibilityContent;

    private JSAccessibilityFactory(String jqueryContent, String accessibilityContent) {
        this.jqueryContent = jqueryContent;
        this.accessibilityContent = accessibilityContent;
    }

    synchronized static JSAccessibilityFactory getInstance() throws IOException {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    static String loadResource(String name) throws IOException {
        String resourceName = "accessibility/" + name;
        LOG.debug("Loading resource: " + resourceName);

        InputStream is = null;
        try {
            is = JSAccessibilityFactory.class.getClassLoader().getResourceAsStream(resourceName);
            if(null == is)
                throw new IOException("Unable to find resource: " + resourceName);
            String content = IOUtils.toString(is);
            return content;
        }
        finally {
            IOUtil.safeClose(is);
        }
    }

    private static JSAccessibilityFactory load() throws IOException {
        String jqueryContent = loadResource(JQUERY_SCRIPT);
        String accessibilityContent = loadResource(AUDIT_SCRIPT);
        return new JSAccessibilityFactory(jqueryContent, accessibilityContent);
    }

    public String getAccessibilityContent() {
        return accessibilityContent;
    }

    public String getJqueryContent() {
        return jqueryContent;
    }
}
