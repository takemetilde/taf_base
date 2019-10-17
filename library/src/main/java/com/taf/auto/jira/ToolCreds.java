package com.taf.auto.jira;

import com.taf.auto.rest.UserPass;

/**
 * Constants for tooling access to JIRA.
 *
 */
public interface ToolCreds {
    String JIRA_USER = "srcCONFLUENCEAPI";

    /** Password no longer stored in repo */
    @Deprecated
    String JIRA_PASS = "REDACTED";

    /** Password no longer stored in repo */
    @Deprecated
    UserPass JIRA_CREDS = new UserPass(JIRA_USER, JIRA_PASS);
}
