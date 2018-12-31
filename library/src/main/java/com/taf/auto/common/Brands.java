package com.taf.auto.common;

/**
 * Codifies the available brands the site may support. Concrete enums in the individual automation projects should implement this interface
 * with the real brands.
 *
 */
public interface Brands {
    public String getDomain();
}
