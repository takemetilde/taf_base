package com.taf.auto.common;

public enum DefaultBrands implements Brands {
    ANTHEM("anthem.com");

    private String domain;

    DefaultBrands(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
