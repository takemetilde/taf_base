package com.taf.auto.jira;

import com.taf.auto.common.PrettyPrinter;
import com.taf.auto.io.JSONUtil;
import com.taf.auto.jira.pojo.*;
import com.taf.auto.jira.request.AbstractRequest;
import com.taf.auto.jira.request.CreateIssueRequest;
import com.taf.auto.json.SparseJsonPojo;
import com.taf.auto.rest.RESTParam;
import com.taf.auto.rest.RESTUtil;
import com.taf.auto.rest.UserPass;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.emory.mathcs.backport.java.util.Collections;
import io.restassured.http.ContentType;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import javafx.util.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.taf.auto.common.PrettyPrinter.prettyList;
import static com.taf.auto.io.JSONUtil.*;
import static com.taf.auto.io.JSONUtil.decode;
import static com.taf.auto.io.JSONUtil.flattenJson;
import static com.taf.auto.io.JSONUtil.namePair2;
import static com.taf.auto.io.JSONUtil.pair;
import static com.taf.auto.io.JSONUtil.pair2;
import static com.taf.auto.io.JSONUtil.pairs2;
import static com.taf.auto.jira.JIRAUtil.Action.*;
import static com.taf.auto.jira.JIRAUtil.Constants.DOMAIN_DEFAULT;
import static com.taf.auto.jira.JIRAUtil.Constants.DOMAIN_KEY;
import static com.taf.auto.rest.RESTUtil.assertStatusCode;
import static com.taf.auto.rest.RESTUtil.get;
import static com.taf.auto.jira.JIRAUtil.Action.*;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Utilities for communicating with JIRA via its REST API.
 *
 */
public class JIRAUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JIRAUtil.class);

    /**
     * The domain used to assemble a JIRA API URL. Defaults to {@link Constants#DOMAIN_KEY}.
     */
    public static final String JIRA_DOMAIN = deriveDomain();

    /**
     * Beginning of the URL to the API
     */
    public static final String JIRA_API_URL = JIRA_DOMAIN + "rest/api/2/";

    /**
     * Beginning of the URL to the Issue API
     */
    public static final String JIRA_ISSUE_API_URL = JIRA_API_URL + "issue/";

    public static final String JIRA_RAVEN_URL = JIRA_DOMAIN + "rest/raven/1.0/";

    public static final String JIRA_RAVEN_API_URL = JIRA_RAVEN_URL + "api";

    /**
     * Beginning of the URL to upload a Test Execution
     */
    public static final String JIRA_RAVEN_IMPORT_URL = JIRA_RAVEN_URL + "import";

    /**
     * Composes constants related to JIRA access.
     */
    public interface Constants {
        /**
         * Pass in a JVM argument using this key to override the JIRA domain
         */
        String DOMAIN_KEY = "auto.jiraDomain";

        /**
         * The default JIRA domain if not overridden via {@link #DOMAIN_KEY}
         */
        String DOMAIN_DEFAULT = "https://jira.anthem.com/";

        /**
         * Optional username for logging into JIRA
         */
        String JIRA_USER = "auto.jiraUser";

        /**
         * Optional password for logging into JIRA
         */
        String JIRA_PASS = "auto.jiraPass";

        /**
         * Optional <b>encrypted</b> password for logging into JIRA
         */
        String JIRA_ENCRYPTED_PASS = "auto.jiraEPass";

        static String wrap(String key) {
            return format("${%s}", key);
        }
    }

    private static String deriveDomain() {
        String domain = System.getProperty(DOMAIN_KEY);
        if (isEmpty(domain)) {
            domain = DOMAIN_DEFAULT;
            LOG.info("No value for: " + DOMAIN_KEY + " defaulting to: " + domain);
        } else {
            if (!domain.endsWith("/")) {
                domain = domain + '/';
                LOG.debug("Added trailing slash to domain.");
            }
            LOG.info("Overriding domain to: " + domain);
        }
        return domain;
    }

    private JIRAUtil() { /** static only */}

    public static void showInBrowser(String issueKey) throws IOException, URISyntaxException {
        URL url = new URL(JIRAUtil.formatBrowseURL(issueKey));
        Desktop.getDesktop().browse(url.toURI());
    }

    public static boolean isIssueClosed(AbstractIssue<?> issue) {
        return IssueWorkflowStatus.Closed.matches(issue.fields.status.name);
    }

    /**
     * Authenticates with JIRA using the provided user and pass.
     *
     * @param username the JIRA username
     * @param password the JIRA password
     * @throws Exception if authentication fails.
     */
    public static void authenticate(String username, String password) throws Exception {
        try {
            useRelaxedHTTPSValidation();
            given().
                    contentType(ContentType.JSON).
                    baseUri(JIRA_API_URL + "mypermissions").
                    auth().preemptive().basic(username, password).
                    get().
                    then().
                    statusCode(200);
        } catch (Throwable t) {
            String msg = "Failed to authenticate";
            LOG.error(msg, t);
            throw new Exception(msg);
        }
    }

    /**
     * Formats the URL for browsing to the Issue specified by the given key.
     *
     * @param key the JIRA key (e.g. ANREIMAGED-13849)
     * @return a URL to browse to the given Issue
     */
    public static String formatBrowseURL(String key) {
        return format("%sbrowse/%s", JIRA_DOMAIN, key);
    }

    public static <F extends Fields> SelfKeyId createIssue(CreateIssueRequest<F> request, UserPass up) throws IOException {
        return createIssue(request, up, Optional.empty());
    }

    public static <F extends Fields> SelfKeyId createIssue(CreateIssueRequest<F> request, UserPass up, Optional<Consumer<F>> additional) throws IOException {
        LOG.debug("createIssue's request: " + request);
        String value = flattenJson(request);

        ExtractableResponse<Response> extracted = given().
                contentType(ContentType.JSON).
                baseUri(JIRA_ISSUE_API_URL).
                auth().preemptive().basic(up.username, up.password).
                body(value).
                post().
                then().extract();

        try {
            return extracted.as(SelfKeyId.class);
        } catch(Exception e) {
            LOG.debug("Failed to extract response", e);
            throw new IOException("Failed to create : " + JSONUtil.prettyPrint(extracted));
        }
    }

    public static byte[] fetchIssue(String key, UserPass up) throws IOException {
        return fetchIssue(key, up.username, up.password);
    }

    public static <T> T fetchIssue(String key, Class<T> clazz, UserPass up) throws IOException {
        byte[] bytes = fetchIssue(key, up.username, up.password);
        return JSONUtil.decode(bytes, clazz);
    }

    public static byte[] fetchIssue(String key, String username, String password) throws IOException {
        LOG.info(format("User: %s is fetching Issue: %s", username, key));
        RestAssuredResponseImpl response = get(formatIssueURI(key), username, password);
        int code = response.getStatusCode();
        LOG.debug("fetchIssue returned status code: " + code);
        switch (code) {
            case 200:
                LOG.debug("Issue fetch successful");
                break;
            case 403:
                throw new IOException(format("User: %s is forbidden to fetch Issue: %s (403 forbidden). Please verify username and password.", username, key));
            default:
                throw new IOException(format("User: %s unable to fetch Issue: %s (%d)", username, key, code));
        }
        byte[] data = response.asByteArray();
        return data;
    }

    public static AbstractIssue[] fetchIssuesByJQL(String jql, UserPass up) throws IOException {
        return fetchIssuesByJQL(jql, up.username, up.password, SearchResults.class);
    }

    public static AbstractIssue[] fetchIssuesByJQL(String jql, String username, String password) throws IOException {
        return fetchIssuesByJQL(jql, username, password, SearchResults.class);
    }

    public static <I extends AbstractIssue> I[] fetchIssuesByJQL(String jql, UserPass up, Class<? extends AbstractSearchResults<I>> clazz) throws IOException {
        return fetchIssuesByJQL(jql, up.username, up.password, clazz);
    }

    private static final int MAX_RESULTS_PER_PAGE = 1000;

    public static <I extends AbstractIssue> I[] fetchIssuesByJQL(String jql, String username, String password, Class<? extends AbstractSearchResults<I>> clazz) throws IOException {
        return fetchIssuesByJQL(jql, MAX_RESULTS_PER_PAGE, username, password, clazz);
    }

    public static <I extends AbstractIssue> I[] fetchIssuesByJQL(String jql, int maxResultsPerPage, String username, String password, Class<? extends AbstractSearchResults<I>> clazz) throws IOException {
        LOG.info("Fetching Issues for JQL: " + jql);

        AbstractSearchResults<I> results = fetchIssuesByJQLHelper(jql, Optional.empty(), maxResultsPerPage, username, password, clazz);
        int total = results.total;
        if(total < maxResultsPerPage) {
            LOG.debug("Results fit within single page.");
            return results.issues;
        }

        int numPages = total / maxResultsPerPage;
        LOG.info("Total number of Issues matching JQL: " + total);
        if(numPages * maxResultsPerPage < total) {
            numPages++;
        }
        LOG.info("Results occupy {} pages.", numPages);

        I[] issues = (I[]) Array.newInstance(results.peekIssueConcreteClass(), total);
        System.arraycopy(results.issues, 0, issues, 0, results.issues.length);

        int startAt = maxResultsPerPage;
        int page = 1;
        while(startAt < total) {
            LOG.info("Fetching page: " + ++page);
            results = fetchIssuesByJQLHelper(jql, Optional.of(Integer.valueOf(startAt)), maxResultsPerPage, username, password, clazz);
            System.arraycopy(results.issues, 0, issues, startAt, results.issues.length);
            startAt += maxResultsPerPage;
        }

        return issues;
    }

    private static <I extends AbstractIssue> AbstractSearchResults<I> fetchIssuesByJQLHelper(String jql, Optional<Integer> startAt, int maxResults, String username, String password, Class<? extends AbstractSearchResults<I>> clazz) throws IOException {
        boolean hasStartAt = startAt.isPresent();
        RESTParam[] params = new RESTParam[hasStartAt ? 3 : 2];
        params[0] = new RESTParam("jql", jql);
        params[1] = new RESTParam("maxResults", Integer.toString(maxResults));
        if(hasStartAt) {
            params[2] = new RESTParam("startAt", startAt.get().toString());
        }
        RestAssuredResponseImpl response = get(JIRA_API_URL + "search", username, password, params);

        try {
            return decode(response.asString().getBytes("UTF-8"), clazz);
        } catch(IOException ioe) {
            String out;
            try {
                out = JSONUtil.prettyPrint(response);
            } catch (JsonProcessingException jpe) {
                LOG.error("Failed to prettyPrint.", jpe);
                out = response.asString();
            }
            LOG.error("Failed to decode:\n{}", out);
            throw ioe;
        }
    }

    public static Issue[] fetchIssuesByFilterID(String filterID, String username, String password) throws IOException {
        return fetchIssuesByFilterID(filterID, username, password, SearchResults.class);
    }

    public static <I extends AbstractIssue> I[] fetchIssuesByFilterID(String filterID, String username, String password, Class<? extends AbstractSearchResults<I>> clazz) throws IOException {
        return fetchIssuesByJQL("filter=" + filterID, username, password, clazz);
    }

    /**
     * Takes an array of Issue keys and builds a comma separated list wrapped in parenthesis
     * suitable for passing in JQL.
     *
     * @param issuesKeys the keys to format
     * @return comma separated list of the keys wrapped in parenthesis
     */
    public static String formatJQLForIssueKeys(String[] issuesKeys) {
        StringBuilder jql = new StringBuilder("key in (");
        for (int i = 0; i < issuesKeys.length; i++) {
            if (i != 0)
                jql.append(',');
            jql.append(issuesKeys[i]);
        }
        jql.append(')');
        return jql.toString();
    }

    public static List<String> fetchProjects(String username, String password) throws IOException {
        LOG.debug("Fetching projects...");
        RestAssuredResponseImpl response = get(JIRA_API_URL + "project/", username, password);
        if(LOG.isDebugEnabled()) {
            response.prettyPrint();
        }
        ObjectMapper mapper = new ObjectMapper();
        Project[] projects = mapper.readValue(response.asByteArray(), Project[].class);
        List<String> keys = new ArrayList<>(projects.length);
        for (Project project : projects)
            keys.add(project.key);
        return keys;
    }

    public static String formatIssueURI(String issueKeyOrID) {
        return JIRA_ISSUE_API_URL + issueKeyOrID;
    }

    public static String formatIssueTransitionURI(String issueKeyOrID) {
        return JIRA_ISSUE_API_URL + issueKeyOrID + "/transitions";
    }

    public static String createMessage(String updating, String message) {
        return format("%s: %s", updating, message);
    }

    public enum Action {
        Set, Add, Edit, Remove;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public static class UnsupportedActionException extends RuntimeException {
        public UnsupportedActionException(Action action) {
            super("Unsupported action: " + action);
        }
    }

    public static String updateBody(Object key, String value) {
        return format("{ \"update\": %s }", pair(key, value));
    }

    public static String updateBody2(Object key, String value) {
        return format("{ \"update\": %s }", pair2(key, value));
    }

    private static String update(String type, Object key, List<String> values) {
        StringBuilder value = new StringBuilder("[");
        value.append(PrettyPrinter.prettyCollection(values, ", "));
        value.append(']');
        return format("{ \"%s\": {\"%s\": %s } }", type, key, value.toString());
    }

    public static String updateBody(Object key, List<String> values) {
        return update("update", key, values);
    }

    public static String updateFields(Object key, String value) {
        return format("{ \"fields\": %s }", pair(key, value));
    }

    public static String updateFields2(Object key, String value) {
        return format("{ \"fields\": %s }", pair2(key, value));
    }

    public static String updateFields(Object key, List<String> values) {
        return update("fields", key, values);
    }

    private static String updateName(String type, Action action, String name) {
        return updateBody(type, format("[ {\"%s\": [ {\"name\": \"%s\"} ] } ]", action, name));
    }

    public static Response put(IssueHandle handle, String body, String message, UserPass up) {
        Response put = RESTUtil.put(handle.getSelf(), body, up);
        LOG.info(message);
        return put;
    }

    private static Response post(IssueHandle handle, String body, String path, String message, UserPass up) {
        Response put = RESTUtil.post(handle.getSelf(), body, path, up);
        LOG.info(message);
        return put;
    }

    public static IssueTypes fetchIssueType(String key, UserPass up) {
        try {
            Response response = get(formatIssueURI(key), up.username, up.password, new RESTParam("fields", "issuetype"));
            int statusCode = response.getStatusCode();
            switch(statusCode) {
                case 200: LOG.debug("Successfully retrieved Issue for key: " + key); break;
                case 404: throw new RuntimeException("Issue not found for key: " + key);
                default: throw new RuntimeException(format("Failed to retrieve Issue for key: %s (%d)", key, statusCode));
            }
            String typeName = response.path("fields.issuetype.name");
            IssueTypes type = IssueTypes.resolve(typeName);
            if(null == type) {
                throw new UnsupportedOperationException("Unexpected Issue type: " + typeName);
            }
            return type;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Issue type for: " + key, e);
        }
    }

    public static LocalDate parseDateTime(String dtVerbose) {
        String pattern = "yyyy-MM-dd";
        String dt = dtVerbose.substring(0, pattern.length());
        return LocalDate.parse(dt, DateTimeFormatter.ofPattern(pattern));
    }

    public static class Summary {
        public static Response update(IssueHandle handle, String summary, UserPass up) {
            return put(handle, updateFields2("summary", summary), createMessage("Summary", summary), up);
        }
    }

    public static class Description {
        public static class Updater extends AbstractRequest {
            @JsonProperty
            public DescUpdater fields = new DescUpdater();
        }

        public static class DescUpdater {
            @JsonProperty
            public String description;
        }

        public static Response update(IssueHandle handle, String description, UserPass up) {
            Updater u = new Updater();
            u.fields.description = description;

            Response response = put(handle, u.toString(), createMessage("Description", description), up);
            return assertStatusCode(response, "Description", 204);
        }
    }


    public static class Environment {
        public static Response update(IssueHandle handle, String env, UserPass up) {
            return put(handle, updateFields2("environment", env), createMessage("Environment", env), up);
        }
    }

    public static class TestEnvironment {
        public static Response update(IssueHandle handle, String env, UserPass up) {
            Response response = put(handle, updateBody("customfield_14725", singletonList(pair2(Add, env))), createMessage("Test Environment", env), up);
            return assertStatusCode(response, "Test Environment", 204);
        }
    }

    public static class Revision {
        public static Response update(IssueHandle handle, String revision, UserPass up) {
            return put(handle, updateBody("customfield_14136", singletonList(pair2(Set, revision))), createMessage("Revision", revision), up);
        }
    }

    public static class Label {
        /**
         * Makes a PUT call to /rest/api/2/issue/{issueKeyOrID} with labels in JSON representation
         * in the request update. Accepts single or comma separated labels. Returns the Response object
         * with 204 status code upon success, 400 if failed, and 403 if user has incorrect permissions.
         *
         * @param handle contains information about the issue
         * @param labels the label(s) to be added.
         * @param up     the username and password of the JIRA user.
         * @return response the Response object received after making the PUT call.
         */
        public static Response add(IssueHandle handle, List<String> labels, UserPass up) {
            Response response = go(handle, labels, "New", up,
                    () -> prettyList(labels.stream()
                            .map(l -> pair2(Add, l))
                            .collect(toList())));
            assertStatusCode(response, "Add Label", 204);
            return response;
        }

        /**
         * Makes a PUT call to /rest/api/2/issue/{issueKeyOrID} with labels in JSON representation
         * in the request update. Returns the Response object with 204 status code upon success, 400 if
         * failed, and 403 if user has incorrect permissions.
         *
         * @param handle contains information about the issue
         * @param label  the label to be removed.
         * @param up     the username and password of the JIRA user.
         * @return response the Response object received after making the PUT call.
         */
        public static Response remove(IssueHandle handle, String label, UserPass up) {
            return go(handle, singletonList(label), "Remove", up,
                    () -> pair2(Remove, label));
        }

        private static Response go(IssueHandle handle, List<String> labels, String message, UserPass up, Supplier<String> mold) {
            return put(handle, updateBody("labels", singletonList(mold.get())), createMessage(message + " Label(s)", prettyList(labels)), up);
        }

        /**
         * Takes a comma separated list of labels and splits them into a list.
         *
         * @param labels the given encoded list
         * @return a first class list of the labels
         */
        public static List<String> split(String labels) {
            try {
                String[] split = labels.split(",");
                List<String> processed = new ArrayList<>(split.length);
                for(String label : split) {
                    processed.add(label.trim());
                }
                return processed;
            } catch (Exception e) {
                throw new RuntimeException("Failed to split labels: " + labels, e);
            }
        }
    }

    public static class Comment {
        /**
         * Makes a POST call to /rest/api/2/issue/{issueIdOrKey}/comments in order to add a comment to
         * a specific issue. Response returns with status code 201 and the new comment in the response
         * body if successful, status code 400 otherwise.
         * <p>
         * Note: the comment passed in will be JSON escaped with {@link JSONUtil#quote(String)}.
         *
         * @param handle  contains information about the issue
         * @param comment the new comment to be added to an issue.
         * @param up      the username and password of the JIRA user
         * @return response     the Response object received after making the PUT call.
         */
        public static Response add(IssueHandle handle, String comment, UserPass up) {
            String quotedComment = JSONUtil.quote(comment);
            Response response = post(handle, pair2("body", quotedComment), "comment", createMessage("Add Comment", comment), up);
            assertStatusCode(response, "Comment", 201);
            return response;
        }

        /**
         * Makes a PUT call to /rest/api/2/issue/{issueIdOrKey}/comment/{id} in order to edit a comment belonging to
         * an issue. Returns the Response object with 204 status code upon success, 400 if failed, and 403 if user
         * has incorrect permissions.
         *
         * @param handle    contains information about the issue
         * @param commentID the ID of the comment to be edited.
         * @param comment   the edited comment to be added to an issue.
         * @param up        the username and password of the JIRA user
         * @return response     the Response object received after making the DELETE call.
         */
        public static Response edit(IssueHandle handle, String commentID, String comment, UserPass up) {
            return go(handle, Edit, commentID, Optional.of(comment), up);
        }

        /**
         * Makes a DELETE call to /rest/api/2/issue/{issueIdOrKey}/comment/{id} in order to delete a comment from
         * an issue. Returns the Response object with 204 status code upon success, 400 if failed, and 403 if user
         * has incorrect permissions.
         *
         * @param handle    contains information about the issue
         * @param commentID the id of the comment to be deleted.
         * @param up        the username and password of the JIRA user
         * @return response     the Response object received after making the DELETE call.
         */
        public static Response remove(IssueHandle handle, String commentID, UserPass up) {
            return go(handle, Remove, commentID, Optional.empty(), up);
        }


        private static Response go(IssueHandle handle, Action action, String commentID, Optional<String> optBody, UserPass up) {
            List<Pair<String, String>> vals = new ArrayList<>(2);
            vals.add(new Pair<>("id", commentID));
            optBody.ifPresent(b -> vals.add(new Pair<>("body", b)));
            String body = updateBody("comment", singletonList(pair(action, pairs2(vals))));
            return put(handle, body, createMessage("Comment", action.name()), up);
        }
    }

    public static class FixVersion {
        /**
         * Makes a PUT call to /rest/api/2/issue/{issueKeyOrID} with Fix Version in JSON representation
         * in the request update. Returns the Response object with 204 status code upon success, 400 if
         * failed, and 403 if user has incorrect permissions.
         *
         * @param handle         contains information about the issue
         * @param fixVersionName the name of the Fix Version to be added.
         * @param up             the username and password of the JIRA user
         * @return response the Response object received after making the PUT call.
         */
        public static Response update(IssueHandle handle, String fixVersionName, UserPass up) {
            return go(handle, fixVersionName, n -> pair(Set, singletonList(namePair2(fixVersionName))), "New", up);
        }

        /**
         * Makes a PUT call to /rest/api/2/issue/{issueKeyOrID} with Fix Version in JSON representation
         * in the request update. Returns the Response object with 204 status code upon success, 400 if
         * failed, and 403 if user has incorrect permissions.
         *
         * @param handle         contains information about the issue
         * @param fixVersionName the name of the Fix Version to be removed.
         * @param up             the username and password of the JIRA user.
         * @return response the Response object received after making the PUT call.
         */
        public static Response remove(IssueHandle handle, String fixVersionName, UserPass up) {
            return go(handle, fixVersionName, n -> pair(Remove, namePair2(fixVersionName)), "Old", up);
        }

        private static Response go(IssueHandle handle, String fixVersionName, Function<String, String> f, String message, UserPass up) {
            String body = updateBody("fixVersions", singletonList(f.apply(fixVersionName)));
            return put(handle, body, createMessage(message + " Fix Version Name", fixVersionName), up);
        }
    }

    public static class ITTeam {
        static class EditMeta {
            static class Fields extends SparseJsonPojo {
                static class Custom12479 extends SparseJsonPojo {
                    @JsonProperty
                    public SelfValueId[] allowedValues;
                }

                @JsonProperty
                public Custom12479 customfield_12479;
            }

            @JsonProperty
            public Fields fields;
        }

        private static final String key = "customfield_12479";

        /**
         * Makes a PUT call to  /rest/api/2/issue/{issueID} in order to associate an issue with a
         * certain IT Team. The allowed values for an issue's IT Team can be viewed at /rest/api/2/issue/{issueID}/editmeta.
         * Returns the Response object with 204 status code upon success, 400 if failed, and 403 if user has incorrect permissions.
         *
         * @param handle contains information about the issue
         * @param team   the IT Team
         * @param up     the username and password of the JIRA user.
         * @return the Response object received after making the PUT call
         */
        public static Response update(IssueHandle handle, ITTeams team, UserPass up) {
            String teamID = Integer.toString(team.id);
            String body = updateFields(key, singletonList(pair2("id", teamID)));
            return put(handle, body, createMessage("IT Team ID", teamID), up);
        }

        public static List<ITTeams> getAll(String project, UserPass up) throws IOException {
            String jql = format("project=%s and status != Closed", project);
            AbstractSearchResults<Issue> issues = fetchIssuesByJQLHelper(jql, Optional.empty(), 1, up.username, up.password, SearchResults.class);
            if(0 == issues.total) {
                LOG.error("No issues came back for Project: {}", project);
                return Collections.emptyList();
            }
            if(issues.total > 1) {
                LOG.warn("Only 1 result expected but got: {}", issues.total);
            }

            Response response = get(JIRA_ISSUE_API_URL + issues.issues[0].key + "/editmeta", up);
            assertStatusCode(response, "editmeta", 200);
            EditMeta meta = decode(response.asByteArray(), EditMeta.class);

            if(null == meta.fields.customfield_12479) {
                LOG.error("No IT Teams found for project: " + project);
                return emptyList();
            }

            List<ITTeams> values = new ArrayList<>();
            for(SelfValueId svi : meta.fields.customfield_12479.allowedValues) {
                values.add(new ITTeams(Integer.parseInt(svi.id), svi.value));
            }
            Collections.sort(values);
            return values;
        }
    }

    public static class Sprint {
        public static Response update(IssueHandle handle, String sprintID, UserPass up) {
            String body = updateBody("customfield_11224", singletonList(pair(Set, sprintID)));
            Response response = put(handle, body, createMessage("Sprint ID", sprintID), up);
            assertStatusCode(response, "Sprint", 204);
            return response;
        }
    }

    public static class Status {
        public static class Update extends AbstractRequest {
            public static class Fields {
                public static class Assignee {
                    @JsonProperty
                    public String name;
                }

                public static class Resolution {
                    @JsonProperty
                    public String name;
                }

                @JsonProperty
                public Assignee assignee;

                @JsonProperty
                public Resolution resolution;
            }

            @JsonProperty
            public Fields fields;

            public static class Transition {
                @JsonProperty
                public String id;
            }

            @JsonProperty
            public Transition transition;
        }

        public static Response update(String issueKey, String statusId, UserPass up) {
            return update(issueKey, statusId, Optional.empty(), Optional.empty(), up);
        }

        public static Response update(String issueKey, String statusId, Optional<String> optResolution, Optional<String> optAssigneeName, UserPass up) {
            Update update = new Update();

            Update.Transition transition = new Update.Transition();
            transition.id = statusId;
            update.transition = transition;

            optResolution.ifPresent(r -> {
                Update.Fields fields = new Update.Fields();
                fields.resolution = new Update.Fields.Resolution();
                fields.resolution.name = r;
                update.fields = fields;
            });

            optAssigneeName.ifPresent(n -> {
                if(null == update.fields) {
                    update.fields =  new Update.Fields();
                }
                update.fields.assignee = new Update.Fields.Assignee();
                update.fields.assignee.name = n;
            });

            Response response = RESTUtil.post(formatIssueTransitionURI(issueKey), update.toString(), "", up);
            assertStatusCode(response, "Status", 204);
            return response;
        }

        public static String get(IssueHandle handle, UserPass up) {
            RestAssuredResponseImpl response = RESTUtil.get(formatIssueTransitionURI(handle.getKey()), up.username, up.password, new RESTParam("expand", "transitions.fields"));
            return response.prettyPrint();
        }

        public static class Transitions extends SparseJsonPojo {
            @JsonProperty
            public Transition[] transitions;
        }

        public static Transitions getTransitions(String issueKey, UserPass up) throws IOException {
            RestAssuredResponseImpl response = RESTUtil.get(JIRA_ISSUE_API_URL + issueKey + "/transitions?transitionId", up.username, up.password);
            assertStatusCode(response, "Transitions", 200);
            return decode(response.asByteArray(), Transitions.class);
        }

        /**
         * Calls {@link #getTransitions(String, UserPass)} to get all transitions available for the given Issue. The
         * first one found matching {@link IssueWorkflowStatus#Closed} is returned.
         *
         * @param issueKey the key to check for
         * @param up user and pass
         * @return the transition for Closed or empty if not found.
         * @throws IOException if problems communicating with JIRA
         */
        public static Optional<Transition> getClosed(String issueKey, UserPass up) throws IOException {
            Transitions transitions = getTransitions(issueKey, up);
            for(Transition t : transitions.transitions) {
                if(IssueWorkflowStatus.Closed.matches(t.name)) {
                    return Optional.of(t);
                }
            }
            return Optional.empty();
        }

        /**
         * Closes the target Issue.
         *
         * @param issueKey the key of the issue
         * @param reason the reason (e.g. "Abandoned")
         * @param optAssigneeName an optional assignee name. Send Optional.of("") to remove the assignee.
         * @param up user and pass
         * @throws IOException if problems communicating with JIRA
         */
        public static void close(String issueKey, String reason, Optional<String> optAssigneeName, UserPass up) throws IOException {
            Optional<Transition> closed = getClosed(issueKey, up);
            if(!closed.isPresent()) {
                String msg = format("Issue: %s does not have a %s status to transition to.", issueKey, IssueWorkflowStatus.Closed);
                throw new IllegalArgumentException("Issue: " + issueKey + " does not have a Closed status to transition to.");
            }
            update(issueKey, closed.get().id, Optional.of(reason), optAssigneeName, up);
        }

        public static void reopenClosed(String issueKey, UserPass up) throws IOException {
            Transitions transitions = getTransitions(issueKey, up);
            for(Transition t : transitions.transitions) {
                if (IssueWorkflowStatus.Reopened.matches(t.name)) {
                    update(issueKey, t.id, up);
                    return;
                }
            }

            LOG.warn("Reopened not available for Issue: {}", issueKey);
        }
    }

    public static class Attachment {
        public static byte[] download(String uri, UserPass up) throws IOException {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            String jira_attachment_authentication = new String(org.apache.commons.codec.binary.Base64.encodeBase64((up.username+":"+up.password).getBytes()));

            try {
                HttpGet httpget = new HttpGet(uri);
                httpget.setHeader("Authorization", "Basic "+jira_attachment_authentication);

                System.out.println("executing request " + httpget.getURI());

                CloseableHttpResponse response = httpclient.execute(httpget);

                int status = response.getStatusLine().getStatusCode();
                if (status >=200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity.isStreaming()) {
                        byte data[] = EntityUtils.toByteArray(entity);
                        return data;
                    }
                }
                throw new IOException("Unable to download with status: " + status);
            } finally {
                try {
                    httpclient.close();
                } catch (Exception e) {
                    LOG.warn("Failed to close httpclient", e);
                }
            }
        }
    }

    public static class IssueLink {
        public static void delete(String linkId, UserPass up) {
            RESTUtil.delete(JIRA_API_URL + "issueLink/" + linkId, up);
        }
    }
}
