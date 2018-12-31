package com.taf.auto.jira.xray;

import com.taf.auto.jira.xray.TestStatusDiagnosisHandler.CommonCause;
import com.taf.auto.jira.xray.pojo.CucumberTestResult;
import cucumber.runtime.CucumberException;
import org.junit.Test;

import java.util.NoSuchElementException;

import static com.taf.auto.jira.xray.TestStatusDiagnosisHandler.processResult;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link TestStatusDiagnosisHandler}.
 */
public class TestStatusDiagnosisHandlerTest {

    private static CucumberTestResult.Step step() {
        return step(null);
    }

    private static CucumberTestResult.Step step(String errorMsg) {
        CucumberTestResult.Step step = new CucumberTestResult.Step();
        step.result = new CucumberTestResult.Result();
        step.result.error_message = errorMsg;
        return step;
    }

    public static class CommonCauseTest {
        private static boolean help(CucumberTestResult.Step step) {
            return processResult(step, CommonCause.values());
        }


        @Test
        public void nominal() {
            CucumberTestResult.Step step = step();
            assertNull(step.result.error_message);
            boolean result = help(step);
            assertFalse(result);
            assertNull(step.result.error_message);
        }

        @Test
        public void notAbleToTestExceptionTest() {
            String errorMsg = NotAbleToTestException.class.getName() + ": Synthetic";
            CucumberTestResult.Step step = step(errorMsg);
            boolean result = help(step);
            assertTrue(result);
            assertEquals(errorMsg, step.result.error_message);
        }
        
        @Test
        public void failedToInstantiateBaseStepsTest() {
        	expectResultAndStatusForEvidence(CucumberException.class.getName() + ": Failed to instantiate class com.taf.steps.core.BaseSteps");
        }
        
        @Test
        public void assertionError() {
        	expectResultAndStatusForEvidence(AssertionError.class.getName() + ": ");
        }
        
        @Test
        public void uniqueElementNotFoundTest() {
        	expectResultAndStatusForEvidence("java.lang.RuntimeException: Unique element for Page ");
        }
        
        @Test
        public void noSuchElementExceptionTest() {
        	expectResultAndStatusForEvidence(NoSuchElementException.class.getName() + ": ");
        }
        
        @Test
        public void failedClickTest() {
        	expectResultAndStatusForEvidence("Other element would receive the click:");
        }
        
        @Test
        public void emptyWebElementExceptionTest() {
        	expectResultAndStatusForEvidence("EmptyWebElementException:");
        }
        
        private static void expectResultAndStatusForEvidence(String errorMsg) {
        	CucumberTestResult.Step step = step(errorMsg);
        	boolean result = help(step);
        	assertTrue(result);
        	assertFalse(step.result.status == null || step.result.status.isEmpty());
        }
        
    }
}
