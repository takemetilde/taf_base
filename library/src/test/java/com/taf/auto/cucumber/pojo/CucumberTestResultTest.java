package com.taf.auto.cucumber.pojo;

import org.junit.Test;

import java.net.URISyntaxException;

import static com.taf.auto.IOUtil.readBytesFromClasspath;
import static com.taf.auto.io.JSONUtil.decode;

/**
 * Unit tests for {@link CucumberTestResult}.
 */
public class CucumberTestResultTest {
    public static String peekTestResultResource() throws URISyntaxException {
        return "/cucumber/test_result.json";
    }

    @Test
    public void testDecode() throws Exception {
        CucumberTestResult[] result = decode(readBytesFromClasspath(peekTestResultResource()), CucumberTestResult[].class);
    }

    @Test
    public void testDecode2() throws Exception {
        CucumberTestResult[] result = decode(readBytesFromClasspath("/cucumber/test_result2.json"), CucumberTestResult[].class);
    }
}
