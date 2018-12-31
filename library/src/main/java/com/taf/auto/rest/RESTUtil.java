package com.taf.auto.rest;

import com.taf.auto.jira.request.AbstractRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;

/**
 * Utilities for making REST calls.
 */
public final class RESTUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RESTUtil.class);

    private RESTUtil() { /** static only */ }

    private static boolean relaxed = false;

    private static void useRelaxedHTTPSValidation() {
        synchronized (RESTUtil.class) {
            if(!relaxed) {
                LOG.trace("Relaxing HTTPS Validation...");
                RestAssured.useRelaxedHTTPSValidation();
                LOG.trace("... relaxed.");
                relaxed = true;
            }
        }
    }

    public static RestAssuredResponseImpl get(String uri, UserPass up) {
        return get(uri, up.username, up.password);
    }

    public static RestAssuredResponseImpl get(String uri, String username, String password) {
        return get(uri, username, password, Optional.empty());
    }

    public static RestAssuredResponseImpl get(String uri, UserPass up, RESTParam... params) {
        return get(uri, up.username, up.password, params);
    }

    public static RestAssuredResponseImpl get(String uri, String username, String password, RESTParam... params) {
        return get(uri, username, password, Optional.of(params));
    }

    private static RestAssuredResponseImpl get(String uri, String username, String password, Optional<RESTParam[]> params) {
        return get(uri, Optional.empty(), Optional.of(new UserPass(username, password)), params);
    }

    public static RestAssuredResponseImpl get(String uri, Optional<Header> header, Optional<RESTParam[]> params) {
        Optional<Headers> headers = header.isPresent() ? Optional.of(new Headers(header.get())) : Optional.empty();
        return get(uri, headers, Optional.empty(), params);
    }

    public static RestAssuredResponseImpl get(String uri, Optional<Headers> headers, Optional<UserPass> up, Optional<RESTParam[]> params) {
        return doThenExtract(RESTAction.GET, uri, headers, up, params);
    }

    public static RestAssuredResponseImpl delete(String uri, UserPass up) {
        return delete(uri, Optional.empty(), Optional.of(up), Optional.empty());
    }

    public static RestAssuredResponseImpl delete(String uri, Optional<Headers> headers, Optional<UserPass> up, Optional<RESTParam[]> params) {
        return doThenExtract(RESTAction.DELETE, uri, headers, up, params);
    }

    private static RestAssuredResponseImpl doThenExtract(RESTAction action, String uri, Optional<Headers> headers, Optional<UserPass> up, Optional<RESTParam[]> params) {
        useRelaxedHTTPSValidation();

        RequestSpecification spec = given().
                contentType(ContentType.JSON).
                baseUri(uri);

        if(headers.isPresent()) {
            spec = spec.headers(headers.get());
        }

        if(params.isPresent()) {
            spec = RESTParam.apply(spec, params.get());
        }

        if(up.isPresent()) {
            UserPass userPass = up.get();
            spec = spec.auth().preemptive().basic(userPass.username, userPass.password);
        }

        return action.apply(spec);
    }

    public static Response put(String uri, AbstractRequest request, UserPass up) {
        return put(uri, request, up);
    }

    public static Response put(String uri, AbstractRequest request, String username, String password) {
        return put(uri, request.toString(), username, password);
    }

    public static Response put(String uri, String body, UserPass up) {
        return put(uri, body, up.username, up.password);
    }

    public static Response put(String uri, String body, String username, String password) {
        return put(uri, body, username, password, ContentType.JSON);
    }

    public static Response put(String uri, String body, String username, String password, ContentType contentType) {
        useRelaxedHTTPSValidation();
        LOG.debug("Put body: " + body);
        Response response =
                given().
                        contentType(contentType).
                        baseUri(uri).
                        auth().preemptive().basic(username, password).
                        body(body).
                        when().
                        put().andReturn();
        if(LOG.isTraceEnabled())
            response.prettyPrint();
        return response;
    }

    public static Response post(String uri, String body, String path, UserPass up) {
        return post(uri, body, path, up.username, up.password);
    }

    public static Response post(String uri, String body, String path, String username, String password) {
        useRelaxedHTTPSValidation();
        Response response = given().
                        contentType(ContentType.JSON).
                        baseUri(uri).
                        auth().preemptive().basic(username, password).
                        body(body).
                        when().
                        post(path);
        return response;
    }

    /**
     * Calls {@link Response#getStatusCode()} on the given response and throws a {@link RuntimeException} if that
     * code is not found in the given codes. If an exception is thrown, the reponse is pretty printed to stdout.
     *
     * @param response the response to get the code
     * @param name the name of the response to include in the possible exception
     * @param validCodes the valid code(s) to check against
     * @return the given response if fluent style is desired
     *
     * @throws RuntimeException if the response code is not found in {@code validCodes}
     */
    public static Response assertStatusCode(Response response, String name, int... validCodes) {
        int code = response.getStatusCode();
        validateStatusCode(code, validCodes).ifPresent(msg -> {
            response.prettyPrint();
            throw new RuntimeException(format("%s has response code: %d and %s", name, code, msg));
        });
        return response;
    }

    /**
     * This method is not intended to be called directly except by unit tests.
     * Instead call the public method {@link #assertStatusCode(Response, String, int...)}.
     *
     * @param code the expected code
     * @param validCodes the valid codes
     * @return {@link Optional#empty()} if valid, else an error message describing the mismatch
     */
    static Optional<String> validateStatusCode(int code, int... validCodes) {
        if(validCodes.length < 1) {
            throw new IllegalArgumentException("codes must have at least one value");
        }
        for(int validCode : validCodes) {
            if(code == validCode) {
                return Optional.empty();
            }
        }
        return Optional.of(validCodes.length == 1 ? "must be: " + validCodes[0] : "must be one of: " + Arrays.toString(validCodes));
    }
}
