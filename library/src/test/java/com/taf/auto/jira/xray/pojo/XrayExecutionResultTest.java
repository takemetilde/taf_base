package com.taf.auto.jira.xray.pojo;

import org.junit.Test;

import static com.taf.auto.IOUtil.readBytesFromClasspath;
import static com.taf.auto.io.JSONUtil.decode;

/**
 * Unit test for {@link XrayExecutionResult}.
 */
public class XrayExecutionResultTest {
    @Test
    public void testDecode() throws Exception {
        XrayExecutionResult result = decode(readBytesFromClasspath("/jira/xray/execution_result.json"), XrayExecutionResult.class);
    }
}
