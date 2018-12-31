package com.taf.auto.jira.xray;

import com.taf.auto.WebDriverUnavailableException;
import com.taf.auto.jira.xray.pojo.CucumberTestResult;
import com.taf.auto.page.AbstractPage.PageObjectInstantiationException;
import cucumber.runtime.CucumberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import static com.taf.auto.jira.xray.XrayTestRunResultStatus.*;

/**
 * Utility for determining the causes of failed test runs
 * 
 * @author AF04021
 */
public class TestStatusDiagnosisHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TestStatusDiagnosisHandler.class);
	
	/**
	 * Examines the given step for any known causes of failure. 
	 * If any known causes are recognized, the step's {@link CucumberTestResult.Step#result result} will have its 
	 * {@code state} and {@code error_message} updated to reflect the cause.
	 * 
	 * @param step the step to examine
	 * @return {@code true} if the given step was updated with a diagnosis, {@code false} otherwise
	 */
	public static boolean processResult(CucumberTestResult.Step step) {
		String stackTrace = step.result.error_message;
		if (null == stackTrace)
			return false;
		
		DiagnosticCheck[] allMatchers = {
			simpleCommonCauseDetection,
		};
		
		return processResult(step, allMatchers);
	}

	/**
	 * Package access to logic for unit testing.
	 *
	 * @param step the step to examine
	 * @param matchers the matchers to consider
	 * @return {@code true} if the given step was updated with a diagnosis, {@code false} otherwise
	 */
	static boolean processResult(CucumberTestResult.Step step, DiagnosticCheck[] matchers) {
		String stackTrace = step.result.error_message;
		if (null == stackTrace)
			return false;

		// First check that recognizes the stack trace gets to set the test run status
		for(DiagnosticCheck matcher : matchers) {
			Optional<Diagnosis> match = matcher.attemptToDiagnose(stackTrace);
			if(match.isPresent()) {
				Diagnosis duple = match.get();
				LOG.info("Diagnosed failure as: " + duple.status.name());
				step.result.status = duple.status.name();
				duple.msg.ifPresent(m -> step.result.error_message = "Automated Failure Diagnosis: " + m
						+ "\n\n" + stackTrace);
				return true;
			}
		}

		// Failed to automatically diagnose the cause of the test failure
		return false;
	}
	
	/**
	 * Duple containing appropriate test run status and a message
	 */
	private static class Diagnosis {
		private final XrayTestRunResultStatus status;
		private final Optional<String> msg;
		
		@SuppressWarnings("unused")
		Diagnosis(XrayTestRunResultStatus status) {
			this(status, null);
		}
		
		public Diagnosis(XrayTestRunResultStatus status, String msg) {
			this.status = status;
			this.msg = Optional.ofNullable(msg);
		}
	}
	
	/**
	 * This functional interface provides the "business logic" for diagnosing test failure causes.
	 * <p>
	 * e.g. "If we see a stack trace like this, it means we had an environmental outage."
	 */
	@FunctionalInterface
	interface DiagnosticCheck {
		 /**
		 * @param stackTrace evidence from test run failure
		 * @return if diagnosis is successful, return the determined failure cause
		 */
		Optional<Diagnosis> attemptToDiagnose(String stackTrace);
	}

	private static final DiagnosticCheck simpleCommonCauseDetection = (stackTrace) -> {
		for (CommonCause cause : CommonCause.values()) {
			Optional<Diagnosis> diagnosis = cause.attemptToDiagnose(stackTrace);
			if (diagnosis.isPresent()) {
				return diagnosis;
			}
		}
		// else return empty.
		return Optional.empty();
	};

	/**
	 * Some known patterns of failure, and their diagnoses
	 * <p>
	 * Add cases here to automatically diagnose failures caused by a known cause
	 */
	public enum CommonCause implements DiagnosticCheck {
		NOT_ABLE_TO_TEST_EXCEPTION(
				startsWithFunc(NotAbleToTestException.class.getName() + ": "),
				NOT_ABLE_TO_TEST,
				null),
		FAILED_TO_INSTANTIATE_BASE_STEPS(
				startsWithFunc(CucumberException.class.getName() + ": Failed to instantiate class com.taf.steps.core.BaseSteps"),
				ABORTED,
				"Failed to instantiate BaseSteps"), // TODO more info here
		FAIL_TO_ESTABLISH_WEBDRIVER(
				startsWithFunc(WebDriverUnavailableException.class.getName() + ": Failed to establish WebDriver for:"),
				ABORTED,
				"Failed to establish WebDriver"),
		LOGIN_FAILURE(
				containsFunc("com.taf.pages.LoginPage.login(LoginPage.java:"),
				ABORTED,
				"Login failure. Most likely an environmental issue."),
		XRAY_FETCH_FAILURE(
				containsFunc("Failed to fetch Xray Tests for"),
				ABORTED,
				"Xray-Fetch failure. The tests were unable to run at all."
				),
		FAILED_TO_INSTANTIATE_PAGE(
				startsWithFunc(PageObjectInstantiationException.class.getName() + ": Failed to instantiate page:"),
				FAIL,
				"Failed to instantiate page. Could be automation code failure, or else environmental instability."),
		ASSERTION_ERROR(
				startsWithFunc(AssertionError.class.getName() + ": "),
				FAIL, // TODO [kss] refine this
				"Assertion failed. If test code is working properly, this could indicate a site defect."),
		UNIQUE_ELEMENT_NOT_FOUND(
				startsWithFunc(RuntimeException.class.getName() + ": Unique element for Page "),
				FAIL, // TODO [kss] refine this, perhaps to ABORTED
				"Timed out while waiting for a page to load. This could indicate environmental instability."),
		NO_SUCH_ELEMENT_EXCEPTION(
				startsWithFunc(NoSuchElementException.class.getName() + ": "),
				FAIL, // TODO [kss] refine this
				"Unable to locate an element on the page"),
		FAILED_CLICK(
				containsFunc("Other element would receive the click:"),
				FAIL, // TODO [kss] refine this
				"Unable to click something"),
		EMPTY_WEB_ELEMENT_EXCEPTION(
				containsFunc("EmptyWebElementException:"),
				FAIL, // TODO [kss] refine this
				"Attempted to perform an operation on an unreachable element"),
		;
		
		private final Function<String,Boolean> test;
		private final XrayTestRunResultStatus diagnosis;
		private final String explanation;
		
		/**
		 * @param matchTest a boolean function to test whether a stack trace was caused by this CommonCause
		 * @param diagnosis Xray status that describes this failure mode
		 * @param explanationMessage string message explaining why the test failed
		 */
		CommonCause(Function<String,Boolean> matchTest, XrayTestRunResultStatus diagnosis, String explanationMessage) {
			this.test = matchTest;
			this.diagnosis = diagnosis;
			this.explanation = explanationMessage;
		}
		
		public Optional<Diagnosis> attemptToDiagnose(String evidence) {
			if (test.apply(evidence)) {
				return Optional.of(new Diagnosis(diagnosis, explanation));
			}
			return Optional.empty();
		}
	}
	
	/**
	 * @param containedString given substring
	 * @return a boolean function to check whether a string contains the given substring
	 */
	private static Function<String, Boolean> containsFunc(String containedString) {
		return str -> str.contains(containedString);
	}
	
	/**
	 * @param startingString
	 * @return a boolean function to check whether a string starts with the given string
	 */
	private static Function<String, Boolean> startsWithFunc(String startingString) {
		return str -> str.startsWith(startingString);
	}
}
