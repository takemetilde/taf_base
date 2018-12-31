package com.taf.auto.steps;

import com.taf.auto.step.MultiStepAssertor;
import org.junit.Test;

/**
 * Test cases for {@link com.taf.auto.step.MultiStepAssertor)
 *
 * Created by AD96317 on 6/29/2016.
 */
public class MultiStepAssertorTest {
    @Test(expected = AssertionError.class)
    public void assertTrueWithFalseResult() {
        MultiStepAssertor assertor = new MultiStepAssertor();

        assertor.addStepAssertTrue("Message", false);

        assertor.assertAll();
    }

    @Test(expected = AssertionError.class)
    public void assertFalseWithTrueResult() {
        MultiStepAssertor assertor = new MultiStepAssertor();

        assertor.addStepAssertFalse("Message", true);

        assertor.assertAll();
    }

    @Test(expected = AssertionError.class)
    public void assertFailWithMessage() {
        MultiStepAssertor assertor = new MultiStepAssertor();

        assertor.addStepFail("Message");

        assertor.assertAll();
    }

    @Test(expected = AssertionError.class)
    public void assertFailWithException() {
        MultiStepAssertor assertor = new MultiStepAssertor();

        assertor.addStepFail(new Exception("Message"));

        assertor.assertAll();
    }

    @Test
    public void noErrorIfNoAssertAllCalled() {
        MultiStepAssertor assertor = new MultiStepAssertor();
        String msg = "Message";

        assertor.addStepFail(msg);
        assertor.addStepFail(new Exception(msg));
        assertor.addStepAssertTrue(msg, true);
        assertor.addStepAssertTrue(msg, false);
        assertor.addStepAssertFalse(msg, true);
        assertor.addStepAssertFalse(msg, false);
    }

    @Test
    public void assertAllWithNoResultsIsSuccessful() {
        MultiStepAssertor assertor = new MultiStepAssertor();

        assertor.assertAll();
    }

    @Test
    public void assertAllWithSuccessfulResultsIsSuccessful() {
        MultiStepAssertor assertor = new MultiStepAssertor();

        assertor.addStepAssertTrue("Assert True", true);
        assertor.addStepAssertFalse("Assert False", false);

        assertor.assertAll();
    }

    @Test
    public void assertAllWithClearedResultsIsSuccessful() {
        MultiStepAssertor assertor = new MultiStepAssertor();

        assertor.addStepAssertTrue("Message", false);
        assertor.clearResults();

        assertor.assertAll();
    }
}
