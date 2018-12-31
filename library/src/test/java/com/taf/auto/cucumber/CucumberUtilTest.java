package com.taf.auto.cucumber;

import com.taf.auto.cucumber.pojo.CucumberTestResult;
import com.taf.auto.io.JSONUtil;
import com.taf.auto.jira.xray.pojo.XrayExecutionTest;
import com.taf.auto.cucumber.pojo.CucumberTestResultTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.taf.auto.IOUtil.readBytesFromClasspath;
import static com.taf.auto.cucumber.CucumberUtil.deriveXrayTestKeyFromURI;
import static com.taf.auto.cucumber.CucumberUtil.extractMergeKey;
import static com.taf.auto.cucumber.pojo.CucumberTestResultTest.peekTestResultResource;
import static com.taf.auto.io.JSONUtil.prettyPrint;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link CucumberUtil}.
 */
public class CucumberUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(CucumberUtilTest.class);

    @Test
    public void testDeriveXrayTestKeyFromURI() {
        String testKey = deriveXrayTestKeyFromURI("com/anthem/auto/ANREIMAGED-20623.feature");
        assertEquals("ANREIMAGED-20623", testKey);
    }

    @Test
    public void testMergeTestResultsToXray() throws Exception {
        CucumberTestResult[] results = JSONUtil.decode(readBytesFromClasspath(CucumberTestResultTest.peekTestResultResource()), CucumberTestResult[].class);
        XrayExecutionTest test = CucumberUtil.extractCucumberToXray(results[0]);
        LOG.info("Result:\n" + prettyPrint(test));
    }

    @Test
    public void testExtractMergeKey() {
        final Optional<Object> empty = Optional.empty();
        assertEquals(empty, extractMergeKey(null));
        assertEquals(empty, extractMergeKey(""));
        assertEquals(empty, extractMergeKey("foo"));
        assertEquals(empty, extractMergeKey("com/anthem/auto/xray-fetch/ANREIMAGED-30749.feature"));
        assertEquals(Optional.of("ANREIMAGED-30749"), extractMergeKey("com/anthem/auto/xray-fetch/ANREIMAGED-30749(0).feature"));
    }
}
