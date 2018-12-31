package com.taf.auto.jira.request;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.taf.auto.io.JSONUtil.prettyPrint;

/**
 * Superclass for requests.
 */
public abstract class AbstractRequest extends SparseJsonPojo {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRequest.class);

    public String toString() {
        try {
            return prettyPrint(this);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to parse: " + this, e);
            return "?";
        }
    }
}
