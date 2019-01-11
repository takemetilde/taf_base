package com.taf.auto;

import static com.taf.auto.HTMLUtil.isLinkBroken;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link HTMLUtil}.
 */
public class HTMLUtilTest {

    //TODO: Offline status needs to be handled here. Temporarily disabled.
    //@Test
    public void validURL() {
        assertFalse(isLinkBroken("http://google.com"));
    }

    //TODO: Offline status needs to be handled here. Temporarily disabled.
    //@Test
    public void brokenLink() {
        assertTrue(isLinkBroken("http://google.com/there_is_no_Way_this_would2222_ever_resolve"));
    }
}
