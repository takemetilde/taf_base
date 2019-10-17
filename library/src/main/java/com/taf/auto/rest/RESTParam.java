package com.taf.auto.rest;

import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Encapsulates a key/value pair to be passed as a parameter.
 *
 */
public class RESTParam {
    private final String key;
    private final String value;

    public RESTParam(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static RequestSpecification apply(RequestSpecification spec, RESTParam[] params) {
        for(RESTParam param : params)
            spec = spec.param(param.key, param.value);
        return spec;
    }

    public static RESTParam[] parse(List<String> paramsToParse) {
        RESTParam[] params = new RESTParam[paramsToParse.size()];
        for(int i = 0; i < params.length; i++) {
            String paramToParse = paramsToParse.get(i);
            String[] split = paramToParse.split(Pattern.quote("="));
            if(split.length != 2) {
                throw new IllegalArgumentException("Parameter to parse does not split into a key=value pair: " + paramToParse);
            }
            params[i] = new RESTParam(split[0], split[1]);
        }
        return params;
    }
}
