package com.taf.auto.rest;

import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.function.Function;

/**
 * Abstracts the application of a particular rest action for use by {@link RESTUtil}.
 *
 * @author AF04261 mmorton
 */
enum RESTAction {
    GET(s -> s.get()),
    DELETE(s -> s.delete());

    private final Function<RequestSpecification, Response> logic;

    RESTAction(Function<RequestSpecification, Response> logic) {
        this.logic = logic;
    }

    RestAssuredResponseImpl apply(RequestSpecification spec) {
        RestAssuredResponseImpl response = (RestAssuredResponseImpl) logic.apply(spec).
                then().
                extract();
        return response;
    }
}
