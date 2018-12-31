package com.taf.auto.jira.pojo.xray;

import com.taf.auto.io.JSONUtil;
import com.taf.auto.jira.ToolCreds;
import com.taf.auto.rest.RESTUtil;
import com.google.common.io.ByteStreams;
import io.restassured.internal.RestAssuredResponseImpl;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.taf.auto.jira.JIRAUtil.formatIssueURI;
import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link XrayTest}.
 */
public class XrayTestTest {
    private static final Logger LOG = LoggerFactory.getLogger(XrayTestTest.class);

    public static void main(String[] args) throws IOException {
        useRelaxedHTTPSValidation();

        String issueKey = "ANREIMAGED-22174";
        RestAssuredResponseImpl response = RESTUtil.get(formatIssueURI(issueKey), ToolCreds.JIRA_USER, ToolCreds.JIRA_PASS);
        System.out.println(response.prettyPrint());
    }

    private static XrayTest load(byte[] data) throws IOException {
        return JSONUtil.decode(data, XrayTest.class);
    }

    private static XrayTest load(String resourceName) throws IOException {
        byte[] data =
                ByteStreams.toByteArray(XrayTestTest.class.getClassLoader().getResourceAsStream(resourceName));
        return load(data);
    }

    @Test @Ignore
    public void test21188() throws IOException {
        XrayTest issue = load("rest/ANREIMAGED-21188.json");

        assertTrue(EqualsBuilder.reflectionEquals(issue, issue));

        File file = Files.createTempFile("21188", ".json").toFile();
        JSONUtil.encode(file, issue);
        file.deleteOnExit();

        XrayTest issue2 = load(Files.readAllBytes(file.toPath()));

        assertTrue(EqualsBuilder.reflectionEquals(issue, issue2));
    }

    @Test @Ignore
    public void test22174() throws IOException {
        XrayTest issue = load("rest/ANREIMAGED-22174.json");
    }

    @Test @Ignore
    public void test12430() throws IOException {
        XrayTest issue = load("rest/ANREIMAGED-12430.json");
    }

    @Test @Ignore
    public void test19303() throws IOException {
        XrayTest issue = load("rest/ANREIMAGED-19303.json");
    }

    @Test @Ignore
    public void test15861() throws IOException {
        XrayTest issue = load("rest/ANREIMAGED-15861.json");
    }

    @Test @Ignore
    public void test14845() throws IOException {
        XrayTest issue = load("rest/ANREIMAGED-14845.json");
    }

    @Test @Ignore
    public void test16101() throws IOException {
        XrayTest issue = load("rest/ANREIMAGED-16101.json");
    }
}
