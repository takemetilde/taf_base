package com.taf.auto.step;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by AD96317 on 6/28/2016.
 *
 * Since it takes a significant amount of time to startup our tests (login, emulate, navigate to the page being tested), it isn't prudent to
 * use the Scenario Outline functionality in Gherkin. This provides a much faster alternative.
 *
 * Instead of a Scenario Outline to perform similar tests, pass all of the parameters to the when method as a list. Perform the testable actions
 * inside of one When step. For each testable action use addStepAssertTrue(...) or addStepAssertFalse(...) to store the results of the tests. Next
 * in the Then step call assertAll(...) to perform every assert that you added in the When step. This will output the results as if it was one
 * assertion and you will be able to see which (if any) of the steps failed.
 */
public class MultiStepAssertor {
    private final ArrayList<StepResult> stepResults;

    public MultiStepAssertor() {
        stepResults = new ArrayList<>();
    }

    /**
     * Add a single assertTrue. Note: No assertion will be performed until assertAll() is called.
     *
     * @param message The message if the assertion fails.
     * @param result The boolean result of the call that is being asserted true.
     */
    public void addStepAssertTrue(String message, boolean result) {
        stepResults.add(new StepResult(message, result));
    }

    /**
     * Add a single assertFalse. Note: No assertion will be performed until assertAll() is called.
     *
     * @param message The message if the assertion fails.
     * @param result The boolean result of the call that is being asserted false.
     */
    public void addStepAssertFalse(String message, boolean result) {
        addStepAssertTrue(message, !result);
    }

    /**
     * Add a single fail. Note: No assertion will be performed until assertAll() is called, and the assertion will
     * fail when it is called, with at least this step failing.
     *
     * @param message The message that will be displayed for this step.
     */
    public void addStepFail(String message) {
        addStepAssertTrue(message, false);
    }

    /**
     * Add a single fail with the exception that was caught for the failure. The message displayed will be {@link Exception#toString()}
     *
     * Note: No assertion will be performed until assertAll() is called, and the assertion will
     * fail when it is called, with at least this step failing.
     *
     * @param exception the exception describing the failure
     */
    public void addStepFail(Exception exception) {
        addStepAssertTrue(exception.toString(), false);
    }

    /**
     * This will call assertAll(String) with "Mutli Step Assertion" as the stepDescription
     */
    public void assertAll() {
        assertAll("Multi Step Assertion");
    }

    /**
     * This will perform every assertion that has been added. Each message for failed assertions will be displayed together in a formatted message.
     *
     * @param stepDescription A description of the step that the assertion is called from. I recommend the gherkin step, or method name.
     */
    public void assertAll(String stepDescription) {
        boolean allResults = true;
        StringBuilder builder = new StringBuilder();
        builder.append("Begin results for ").append(stepDescription).append("\n\n");

        for (StepResult stepResult : this.stepResults) {
            if (!stepResult.success) {
                allResults = false;
                builder.append(stepResult.assertMessage).append("\n");
            }
        }

        builder.append("\nEnd results for ").append(stepDescription).append(".");

        assertTrue(builder.toString(), allResults);
    }

    /**
     * Remove all prevoiusly added assertions.
     */
    public void clearResults() {
        stepResults.clear();
    }

    private static class StepResult {
        private String assertMessage;
        private boolean success;

        public StepResult(String assertMessage, boolean success) {
            this.assertMessage = assertMessage;
            this.success = success;
        }
    }
}

