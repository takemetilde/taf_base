package com.taf.auto.jira;

/**
 * Codifies the Issue types to expect from JIRA.
 */
public enum IssueTypes {
    Epic,
    Story,
    Task,
    Test,
    Test_Execution,
    Test_Plan,
    Test_Set,
    Technical_task,
    Defect;

    public String toString() {
        return super.toString().replace("_", " ");
    }

    public static IssueTypes resolve(String name) {
        return valueOf(name.replace(" ", "_"));
    }
}
