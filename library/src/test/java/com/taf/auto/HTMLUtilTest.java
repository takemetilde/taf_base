package com.taf.auto;

import com.taf.auto.HTMLUtil;
import org.junit.Test;

import static com.taf.auto.HTMLUtil.isLinkBroken;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link HTMLUtil}.
 *
 * @author AF04261 mmorton
 */
public class HTMLUtilTest {

    @Test
    public void validURL() {
        assertFalse(isLinkBroken("http://google.com"));
    }

    @Test
    public void brokenLink() {
        assertTrue(isLinkBroken("http://google.com/there_is_no_Way_this_would2222_ever_resolve"));
    }
}
