package com.taf.auto.jira.xray;

/**
 * Codify the test run result statuses.
 *
 */
public enum XrayTestRunResultStatus {
	
	/** Test passed */
	PASS,
	
	/** Unexecuted */
	TODO,
	
	/** Currently executing */
	EXECUTING,
	
	/** Undiagnosed failure */
	FAIL,
	
	/**
	 * [kss] 6/16/17 Currently intended to mean "Environmental or other failures"
	 */
	ABORTED,
	
	/** Application code failure */
	PENDING_DEFECT_FIX,
	
    /** Insufficient test data */
    PENDING_TEST_DATA,
    
    /**
     * [kss] 6/16/17 Currently intended to mean "Automation code failure"
     */
    NOT_ABLE_TO_TEST,
    
    /**
     * Application code has not yet been completed
     */
    NOT_DELIVERED
    
}
