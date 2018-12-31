package com.taf.auto.bamboo;

import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.rest.RESTUtil;
import com.taf.auto.rest.UserPass;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.taf.auto.bamboo.BambooUtil.Constants.DOMAIN_DEFAULT;
import static com.taf.auto.bamboo.BambooUtil.Constants.DOMAIN_KEY;
import static com.taf.auto.io.JSONUtil.pair2;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Utilities for communicating with Bamboo via its REST API.
 *
 * @author AF04261 mmorton
 */
public final class BambooUtil {
    private static final Logger LOG = LoggerFactory.getLogger(BambooUtil.class);

    /**
     * The domain used to assemble a JIRA API URL. Defaults to {@link JIRAUtil.Constants#DOMAIN_KEY}.
     */
    public static final String BAMBOO_DOMAIN = deriveDomain();

    /**
     * Beginning of the URL to the API
     */
    public static final String BAMBOO_API_URL = BAMBOO_DOMAIN + "rest/api/latest/";

    /**
     * Beginning of the URL to the Issue API
     */
    public static final String BAMBOO_RESULT_API_URL = BAMBOO_API_URL + "result/";

    public interface Constants {
        /**
         * Pass in a JVM argument using this key to override the JIRA domain
         */
        String DOMAIN_KEY = "auto.bambooDomain";

        /**
         * The default JIRA domain if not overridden via {@link #DOMAIN_KEY}
         */
        String DOMAIN_DEFAULT = "https://bamboo.anthem.com/";
    }

    private static String deriveDomain() {
        String domain = System.getProperty(DOMAIN_KEY);
        if (isEmpty(domain)) {
            domain = DOMAIN_DEFAULT;
            LOG.info("No value for: " + DOMAIN_KEY + " defaulting to: " + domain);
        } else {
            if (!domain.endsWith("/")) {
                domain = domain + '/';
                LOG.debug("Added trailing slash to domain.");
            }
            LOG.info("Overriding domain to: " + domain);
        }
        return domain;
    }

    private BambooUtil() { /** static only */}

    public static class Comment {
        /**
         * Adds a comment to the job result.
         *
         * @param jobKey the result to modify <code>projectkey-plankey-jobnum</code> e.g. ANREIMAGED-DEVBR-126
         * @param comment the comment to add
         * @param up the username and password of the Bamboo user
         * @return the Response object received after making the call.
         */
        public static Response add(String jobKey, String comment, UserPass up) {
            String uri = BAMBOO_RESULT_API_URL;
            String body = pair2("content", comment);
            return RESTUtil.post(uri, body, jobKey + "/comment", up);
        }
    }
}
