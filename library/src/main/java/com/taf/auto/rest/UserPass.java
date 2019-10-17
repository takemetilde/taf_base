package com.taf.auto.rest;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import static com.taf.auto.jira.JIRAUtil.Constants.*;

/**
 * Encapsulates a username and password pair.
 *
 */
public final class UserPass {
    private static final Logger LOG = LoggerFactory.getLogger(UserPass.class);

    public final String username;
    public final String password;

    public UserPass(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Considers the given password and given encrypted password to determine which
     * is applicable. If both are provided, the normal one is used.
     *
     * @param password potential unencrypted password
     * @param encryptedPassword potential encrypted password
     * @param decrypter the decrypter logic
     * @return the resolved password
     * @throws Exception if unable to decrypt
     */
    public static String resolvePassword(String password, String encryptedPassword, SecDispatcher decrypter) throws Exception {
        boolean hasPassword = isDefined(password, wrap(JIRA_PASS));
        boolean hasEncryptedPassword = isDefined(encryptedPassword, wrap(JIRA_ENCRYPTED_PASS));
        if(!hasPassword && !hasEncryptedPassword)
            throw new Exception("Neither password nor encryptedPassword is defined.");
        if(hasEncryptedPassword) {
            try {
                LOG.debug("Decrypting password...");
                String decrypted = decrypter.decrypt(encryptedPassword);
                LOG.debug("... decrypted password.");
                return decrypted;
            } catch (SecDispatcherException e) {
                throw new Exception("Unable to decrypt password", e);
            }
        }
        // else
        LOG.debug("Password not encrypted.");
        return password;
    }

    public static boolean isUsernameDefined(String username) {
        return isDefined(username, wrap(JIRA_USER));
    }

    private static boolean isDefined(String prop, String defaultValue) {
        LOG.trace("Checking: " + prop);
        return null != prop && !prop.equals(defaultValue);
    }

    public static SecDispatcher buildDecrypter(Context context) throws ContextException {
        LOG.debug("Establishing decrypter");
        PlexusContainer container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        try {
            SecDispatcher decrypter = (SecDispatcher) container.lookup(SecDispatcher.class.getName());
            String configurationFile = "~/.m2/settings-security.xml";
            LOG.debug("Overriding configuration file to: " + configurationFile);
            ((DefaultSecDispatcher) decrypter).setConfigurationFile(configurationFile);
            return decrypter;
        } catch(ClassCastException cce) {
            throw new RuntimeException("Decrypter not the expected class: " + DefaultSecDispatcher.class.getName());
        } catch (ComponentLookupException e) {
            throw new ContextException("Decrypter", e);
        }
    }

    private static String glean(String key) {
        String val = System.getProperty(key);
        if(null == val) {
            String msg = String.format("\"%s\" not found in System properties. Add the following to the command line or as an IDE parameter and try again: -D%s=<value>", key, key);
            throw new RuntimeException(msg);
        }
        return val;
    }

    /**
     * Attempt to glean a valid username and password from the System properties. If either username or password is not
     * found a runtime is thrown.
     *
     * @return a username and password pair
     */
    public static UserPass glean() {
        String username = glean("auto.jiraUser");
        String password = glean("auto.jiraPass");
        return new UserPass(username, password);
    }
}
