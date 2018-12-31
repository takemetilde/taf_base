package com.taf.auto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Convenience superclass for a POJO to extend to inherit the behavior to ignore unknown properties
 * when deserializing from JSON.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SparseJsonPojo {
}
