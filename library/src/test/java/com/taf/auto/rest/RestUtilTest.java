package com.taf.auto.rest;

import io.restassured.response.Response;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.taf.auto.rest.RESTUtil.validateStatusCode;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link RESTUtil}.
 *
 */
public class RestUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(RestUtilTest.class);

    public static class ResponseTest {
        public static void assertResponseCode(Response response, int expectedCode) {
            try {
                assertEquals(response.statusCode(), expectedCode);
            } catch (AssertionError e) {
                LOG.error("Response:\n" + response.prettyPrint());
                throw e;
            }
        }

        public static void assertResponse200(Response response) {
            assertResponseCode(response, 200);
        }

        public static void assertResponse201(Response response) {
            assertResponseCode(response, 201);
        }

        public static void assertResponse204(Response response) { assertResponseCode(response, 204); }
    }

    @Test
    public void validateStatusCodeTest() {
        assertEquals("must be: 200", validateStatusCode(400, 200).get());
        assertEquals("must be one of: [200, 204]", validateStatusCode(400, 200, 204).get());
    }
}
