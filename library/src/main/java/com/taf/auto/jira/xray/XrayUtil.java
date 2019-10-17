package com.taf.auto.jira.xray;

import com.taf.auto.IOUtil;
import com.taf.auto.StringUtil;
import com.taf.auto.jira.ITTeams;
import com.taf.auto.jira.IssueHandle;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.pojo.*;
import com.taf.auto.jira.pojo.xray.*;
import com.taf.auto.jira.request.AbstractRequest;
import com.taf.auto.jira.request.CreateXrayTestRequest;
import com.taf.auto.jira.request.LinkJIRAIssueRequest;
import com.taf.auto.jira.request.UpdateJIRAIssueLabelsRequest;
import com.taf.auto.jira.xray.pojo.XrayExecutionResult;
import com.taf.auto.jira.xray.pojo.XrayTestExecutionTest;
import com.taf.auto.jira.xray.pojo.XrayTestRun;
import com.taf.auto.jira.xray.request.XrayUpdateTestRequest;
import com.taf.auto.rest.RESTUtil;
import com.taf.auto.rest.UserPass;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.response.Response;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.taf.auto.IOUtil.NL;
import static com.taf.auto.common.PrettyPrinter.prettyArray;
import static com.taf.auto.common.PrettyPrinter.prettyList;
import static com.taf.auto.io.JSONUtil.*;
import static com.taf.auto.jira.JIRAUtil.*;
import static com.taf.auto.rest.RESTUtil.*;
import static java.lang.String.format;

/**
 * Utilties for working with Xray via the JIRA REST API.
 *
 */
public final class XrayUtil {
    private static final Logger LOG = LoggerFactory.getLogger(XrayUtil.class);

    private XrayUtil() { /** static only */ }

    public static XrayTest fetchTest(String testKey, UserPass up) throws IOException {
        return fetchIssue(testKey, XrayTest.class, up);
    }

    public static String[] fetchTestKeysByTestSet(String testSetKey, String username, String password) throws IOException {
        byte[] data = fetchIssue(testSetKey, username, password);
        XrayTestSet testSet;
        try {
            testSet = decode(data, XrayTestSet.class);
        } catch(IOException ioe) {
            LOG.error("Failed to decode XrayTestSet:\n" + new String(data));
            throw ioe;
        }
        return testSet.fields.tests;
    }

    public static XrayTest[] fetchTestsByTestSet(String testSetKey, String username, String password) throws IOException {
        LOG.debug("Fetching Issues for Test Set: " + testSetKey);
        String[] tests = fetchTestKeysByTestSet(testSetKey, username, password);
        if(tests.length == 0) {
            throw new IOException(format("Test Set %s does not have any Tests", testSetKey));
        }
        return fetchIssuesByJQL(formatJQLForIssueKeys(tests), username, password, XrayTestSearchResults.class);
    }

    public static XrayTest[] fetchTestsByTestPlan(String testPlanKey, String username, String password) throws IOException {
        byte[] data = fetchIssue(testPlanKey, username, password);
        XrayTestPlan testPlan;
        try {
            testPlan = decode(data, XrayTestPlan.class);
        } catch(IOException ioe) {
            LOG.error("Failed to decode XrayTestPlan:\n" + new String(data));

            throw ioe;
        }
        LOG.debug("Fetching Issues for Test Plan: " + testPlanKey);
        String[] tests = testPlan.fields.tests;
        if(tests.length == 0) {
            throw new IOException(format("Test Plan %s does not have any Tests", testPlanKey));
        }
        return fetchIssuesByJQL(formatJQLForIssueKeys(tests), username, password, XrayTestSearchResults.class);
    }

    public static XrayTest[] fetchTestsByJQL(String jql, UserPass up) throws IOException {
        return fetchTestsByJQL(jql, up.username, up.password);
    }

    public static XrayTest[] fetchTestsByJQL(String jql, String username, String password) throws IOException {
        return fetchIssuesByJQL(jql, username, password, XrayTestSearchResults.class);
    }

    public static XrayTest[] fetchTestsByFilterID(String filterID, String username, String password) throws IOException {
        return fetchIssuesByFilterID(filterID, username, password, XrayTestSearchResults.class);
    }

    public static SelfKeyId createXrayTest(String assignee, String projectKey, ScenarioForXray scenario, String username, String password) throws Exception {
        return createXrayTest(assignee, projectKey, scenario, Optional.empty(), Optional.empty(), username, password);
    }

    public static SelfKeyId createXrayTest(String assignee, String projectKey, ScenarioForXray scenario, Optional<XrayTestStatuses> status,
                                           Optional<ITTeams> team, String username, String password) throws Exception {
        LOG.debug("Creating Xray Test...");

        CreateXrayTestRequest request = new CreateXrayTestRequest(assignee, projectKey, scenario.getScenarioName(),
                scenario.getTags(), scenario.getJiraIds(), scenario.getContent(), scenario.getType());
        team.ifPresent(t -> request.fields.IT_Team = SelfValueId.viaIds(t.id));

        SelfKeyId response = JIRAUtil.createIssue(request, new UserPass(username, password));

        List<Pair<String, IssueLinks.LinkVerb>> keyVerbPairs = new ArrayList<>();
        for(String link : request.getAdditionalLinks()) {
            keyVerbPairs.add(new Pair<>(link, IssueLinks.LinkVerb.Tests));
        }
        createAdditionalLinks(response.key, keyVerbPairs, username, password);

        status.ifPresent(s -> {
            try {
                JIRAUtil.Status.update(response.key, s.status.id, new UserPass(username, password));
            } catch (Exception e) {
                LOG.error(format("Failed to update: %s with status: %s", response.key, s));
            }
        });

        return response;
    }

    public static void createAdditionalLink(String testKey, String targetKey, IssueLinks.LinkVerb verb, UserPass up) {
        createAdditionalLinks(testKey, Collections.singletonList(new Pair<>(targetKey, verb)), up.username, up.password);
    }

    public static void createAdditionalLinks(String testKey, List<Pair<String, IssueLinks.LinkVerb>> keyVerbPairs, String username, String password) {
        keyVerbPairs.forEach(kvp -> {
            String link = kvp.getKey();
            try {
                LOG.debug("Creating additional link to: " + link);
                LinkJIRAIssueRequest request = new LinkJIRAIssueRequest(link, kvp.getValue());
                LOG.debug("LinkJIRAIssueRequest: " + request);

                String uri = formatIssueURI(testKey);
                LOG.debug("LinkJIRAIssueRequest URI: " + uri);

                Response response = put(uri, request.toString(), username, password);
                LOG.debug("Additional link result: " + response.prettyPrint());
            } catch(Exception e) {
                String msg = "Failed to create link from: " + testKey + " to: " + link;
                LOG.error(msg, e);
            }

        });
    }

    public static void updateXrayTestWithSelfReferenceLabel(String testKey, String username, String password) throws JsonProcessingException {
        LOG.debug("updateXrayTestWithSelfReferenceLabel");
        UpdateJIRAIssueLabelsRequest request = new UpdateJIRAIssueLabelsRequest(new String[] { testKey });
        LOG.debug("UpdateJIRAIssueLabelsRequest: " + request);

        String uri = formatIssueURI(testKey);
        LOG.debug("UpdateJIRAIssueLabelsRequest URI: " + uri);

        Response response = put(uri, request, username, password);
        LOG.debug("Update result: " + response.prettyPrint());
    }

    /**
     * Imports a Cucumber report as a Xray Test Execution.
     *
     * @param cucumberJson the JSON to upload
     * @param up           the username and password of the JIRA user
     * @return the response
     */
    public static IssueResponse uploadXrayJsonToJira(String cucumberJson, UserPass up) {
        Response response = post(JIRA_RAVEN_IMPORT_URL, cucumberJson, "/execution/cucumber", up);
        IssueResponse response1;
        try {
            response1 = response.then().statusCode(200).extract().as(IssueResponse.class);
        } catch(AssertionError ae) {
            LOG.error("Unexpected response in uploadXrayJsonToJira.");
            response.prettyPrint();
            throw ae;
        }

        LOG.info("Created Test Execution via Cucumber JSON output: " + formatBrowseURL(response1.peekHandle().getKey()));
        return response1;
    }

    public static IssueResponse uploadXrayExecutionResult(XrayExecutionResult result, UserPass up) throws JsonProcessingException {
        if(LOG.isDebugEnabled())
            LOG.debug("Posting result:\n" + prettyPrint(result));
        Response response = post(JIRA_RAVEN_IMPORT_URL, flattenJson(result), "/execution", up);
        try {
            IssueResponse response1 = response.then().statusCode(200).extract().as(IssueResponse.class);
            return response1;
        } catch (AssertionError e) {
            LOG.error("Failed to post:\n" + prettyPrint(result));
            LOG.error("Response: " + response.prettyPrint());
            throw e;
        }
    }

    public static void updateScenario(ScenarioForXray scenario, IssueHandle handle, UserPass up) {
        updateScenario(scenario, handle, Optional.empty(), up);
    }

    public static void updateScenario(ScenarioForXray scenario, IssueHandle handle, Optional<String[]> optPreConditions, UserPass up ) {
        XrayUpdateTestRequest request = new XrayUpdateTestRequest(scenario.getType(), scenario.getContent());
        optPreConditions.ifPresent(preConditions -> request.fields.preConditions = preConditions);
        updateScenario(handle.getKey(), request, up);
    }

    private static void updateScenario(String testKey, XrayUpdateTestRequest request, UserPass up) {
        String body = request.toString();
        Response response = put(formatIssueURI(testKey), body, up);
        try {
            response.then().statusCode(204);
        } catch (AssertionError e) {
            LOG.error("Failed to put:\n" + body);
            LOG.error("Response: " + response.prettyPrint());
            throw e;
        }
    }

    public static class Test {
        public static XrayTestRun[] getTestRuns(String testKey, UserPass up) throws IOException {
            String url = String.format("%s/test/%s/testruns", JIRA_RAVEN_API_URL, testKey);
            RestAssuredResponseImpl response = get(url, up);
            assertStatusCode(response, "getTestRuns", 200);
            return decode(response.asByteArray(), XrayTestRun[].class);
        }
    }

    public static class AddExecutionToPlanRequest extends AbstractRequest {
        @JsonProperty
        public String[] add;
    }

    public static void linkTestExecutionToTestPlan(String testExecutionKey, String testPlanKey, UserPass up) {
        AddExecutionToPlanRequest request = new AddExecutionToPlanRequest();
        request.add = new String[] {testExecutionKey};
        Response response = post(JIRA_RAVEN_API_URL, request.toString(), "testplan/" + testPlanKey + "/testexecution", up);
        try {
            response.then().statusCode(200);
        } catch (AssertionError e) {
            LOG.error("Failed to put:\n" + request);
            LOG.error("Response: " + response.prettyPrint());
            throw e;
        }
    }

    public static class TestExecution {
        private static String formatURI(String testExecutionKey) {
            return format("%s/testexec/%s/test", JIRA_RAVEN_API_URL, testExecutionKey);
        }

        public static XrayTestExecutionTest[] getTests(String testExecutionKey, UserPass up) throws IOException {
            RestAssuredResponseImpl response = get(formatURI(testExecutionKey), up);
            assertStatusCode(response, "TestExecutionTests", 200);
            return decode(response.asByteArray(), XrayTestExecutionTest[].class);
        }
    }

    public static class TestRun {
        private static String formatURI(int testRunId) {
            return format("%s/testrun/%d/comment", JIRA_RAVEN_API_URL, testRunId);
        }

        public static void setComment(int testRunId, String comment, UserPass up) {
            Response response = put(formatURI(testRunId), comment, up);
            assertStatusCode(response, "TestRunComment", 200);
        }
    }

    public static class Scenario {
        public static class Updater extends AbstractRequest {
            @JsonProperty
            public ScenarioUpdater fields = new ScenarioUpdater();
        }

        public static class ScenarioUpdater {
            @JsonProperty(value = "customfield_14122")
            public String cucumberScenario;
        }

        public static Response update(IssueHandle testHandle, String cucumberScenario, UserPass up) {
            Updater u = new Updater();
            u.fields.cucumberScenario = cucumberScenario;

            Response response = JIRAUtil.put(testHandle, u.toString(), JIRAUtil.createMessage("Cucumber Scenario", cucumberScenario), up);
            return assertStatusCode(response, "Cucumber Scenario", 204);
        }
    }

    public static class PreCondition {
        /** A {@link XrayPreCondition} that is modeling a User must have this label */
        public static final String TEST_USER_LABEL = "TestUser";

        public static final String[] TEST_USER_LABEL_AS_ARRAY = { "TestUser" };

        public static boolean isTestUser(XrayPreCondition preCondition) {
            if(null == preCondition.fields.labels) {
                return false;
            }
            for(String label : preCondition.fields.labels) {
                if(TEST_USER_LABEL.equals(label)) {
                    return true;
                }
            }
            return false;
        }

        public static List<XrayPreCondition> fetchUsers(XrayTest test, UserPass up) throws IOException {
            if(null == test.fields.preConditions || test.fields.preConditions.length < 1) {
                return Collections.emptyList();
            }
            List<XrayPreCondition> users = new ArrayList<>(test.fields.preConditions.length);
            XrayPreCondition[] preConditions = get(test.fields.preConditions, up);
            for(XrayPreCondition preCon : preConditions) {
                if(isTestUser(preCon)) {
                    users.add(preCon);
                }
            }
            return users;
        }

        public static String formatUserSummary(String username, String environment, String description) {
            return format("%s - %s - %s", environment, username, description);
        }

        public static XrayPreCondition[] get(String[] keys, UserPass up) throws IOException {
            return fetchIssuesByJQL(formatJQLForIssueKeys(keys), up, XrayPreConditionSearchResults.class);
        }

        public static class Updater extends AbstractRequest {
            @JsonProperty
            public PreConditionUpdater fields = new PreConditionUpdater();
        }

        public static class PreConditionUpdater {
            @JsonProperty(value = "customfield_14127")
            public String[] preConditions;
        }

        public static Response update(IssueHandle testHandle, String[] preConditionKeys, UserPass up) {
            Updater u = new Updater();
            u.fields.preConditions = preConditionKeys;

            Response response = JIRAUtil.put(testHandle, u.toString(), JIRAUtil.createMessage("PreConditions", prettyArray(preConditionKeys)), up);
            return assertStatusCode(response, "PreConditions", 204);
        }

        /**
         * Creates a Pre-Condition that models a User. The Summary will be composed of the ennvironment, username,
         * and description by calling {@link #formatUserSummary(String, String, String)}.
         *
         * @param projectKey the project
         * @param username the name of the user
         * @param environment the environment
         * @param description the description
         * @param attributes the attributes to be placed in the description
         * @param condition the gherkin condition
         * @param up he username and password of the JIRA user
         * @throws IOException if unable to communicate with JIRA
         */
        public static void createUser(String projectKey, String username, String environment, String description,
                                      List<String> attributes, String condition, UserPass up) throws IOException {
            XrayPreCondition preCondition = new XrayPreCondition();
            preCondition.fields = new XrayPreConditionFields();
            preCondition.fields.project = new Project(projectKey);
            preCondition.fields.issuetype = new IssueType("Pre-Condition");
            preCondition.fields.summary = formatUserSummary(username, environment, description);
            preCondition.fields.environment = environment;
            preCondition.fields.description = formatAttributes(attributes);
            preCondition.fields.labels = TEST_USER_LABEL_AS_ARRAY;
            preCondition.fields.preConType = new SelfValueId("Cucumber");
            preCondition.fields.conditions = condition;

            // query JIRA to make sure a user with this name doesn't already exist
            if(userExists(projectKey, username, environment, up)) {
                LOG.warn("Did not create User because a matching one already exists");
                return;
            }

            String body = flattenJson(preCondition);
            Response response = post(JIRA_ISSUE_API_URL, body, "", up);
            assertStatusCode(response, "Create Pre-Condition", 201);
            try {
                LOG.info("Created User: " + formatBrowseURL(response.path("key")));
            } catch(Throwable t) {
                LOG.debug("Failed to extract response", t);
                response.prettyPrint();
            }
        }

        public static boolean userExists(String projectKey, String username, String environment, UserPass up) throws IOException {
            String summary = formatUserSummary(username, environment, "").replace("-", "\\\\-");
            String jql = format("project=%s and type = Pre-Condition and labels = TestUser and summary ~ \"%s\"",
                    projectKey, summary);
            AbstractIssue[] issues = JIRAUtil.fetchIssuesByJQL(jql, up);
            return issues.length > 0;
        }

        public static String formatAttributes(List<String> attributes) {
            List<String> wrapped = attributes.stream().map(attr -> format("| %s |", attr)).collect(Collectors.toList());
            return "Attributes:\n" + prettyList(wrapped, NL);
        }

        /**
         * Examines in given preCondition to see if it contains the expected label {@link #TEST_USER_LABEL}.
         * @param preCondition what to check
         * @return whether the label is present
         */
        public static boolean representsUser(XrayPreCondition preCondition) {
            if(null != preCondition.fields.labels) {
                for(String label : preCondition.fields.labels) {
                    if(TEST_USER_LABEL.equals(label)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public static List<XrayPreCondition> filterTestUsers(XrayPreCondition[] preConditions, String environment) {
            if("AQA".equalsIgnoreCase(environment)) {
                LOG.warn("Converted AQA reference to DEV");
                environment = "DEV";
            }

            LOG.info(format("Inspecting %d Pre-Conditions for Test Users in Environment: %s", preConditions.length, environment));
            List<XrayPreCondition> users = new ArrayList<>(preConditions.length);
            for(XrayPreCondition preCon : preConditions) {
                if(PreCondition.representsUser(preCon)) {
                    if(environment.equals(preCon.fields.environment)) {
                        LOG.info(format("Adding TestUser: %s for Environment: %s", preCon.key, environment));
                        users.add(preCon);
                    } else {
                        LOG.debug(format("Ignoring TestUser: %s because Environment: %s does not match: %s", preCon.key, preCon.fields.environment, environment));
                    }
                } else {
                    LOG.debug(format("Ignoring non %s precondition: %s", TEST_USER_LABEL, preCon.key));
                }
            }
            return users;
        }

        public static boolean hasAtLeastOneTestUser(XrayPreCondition[] preConditions) {
            for(XrayPreCondition preCon : preConditions) {
                if (PreCondition.representsUser(preCon)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean skip;

        private static void extract(Path path) throws IOException {
            Path dest = Paths.get("fetched-attributes.txt");
            Set<String> attrs = new HashSet<>();
            skip = true;
            Files.walk(path).forEach(p -> {
                if(skip) {
                    skip = false;
                    return;
                }
                try {
                    LOG.debug("Decoding: " + p);
                    XrayTest test = decode(p, XrayTest.class);
                    String[] lines = StringUtil.splitNewlines(test.fields.cucumberScenario);
                    int i = 0;
                    while(i < lines.length) {
                        if(lines[i++].contains("member is logged in as user with attributes")) {
                            break;
                        }
                    }
                    Set<String> vars = new HashSet<>();
                    boolean hasExamples = false;
                    while(i < lines.length) {
                        String line = lines[i++].trim();

                        if(line.startsWith("Examples")) {
                            hasExamples = true;
                            break;
                        }

                        if(line.startsWith("|")) {
                            line = line.replace("|", "").trim();
                            if(line.startsWith("<")) {
                                vars.add(line.replace("<", ""));
                            } else {
                                attrs.add(line);
                            }
                        } else {
                            break;
                        }
                    }

                    if(hasExamples) {
                        List<Integer> cols = new ArrayList<>();
                        String[] headers = lines[i++].split("\\|");
                        for(int c = 0; c < headers.length; c++) {
                            if(vars.contains(headers[c].trim())) {

                            }
                        }
                    }

                } catch (IOException e) {
                    LOG.error("Failed to process: " + p);
                }
            });
            List<String> sorted = new ArrayList<>(attrs);
            Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
            LOG.info(format("Wrote %d attributes", sorted.size()));
            Files.write(dest, prettyList(sorted, NL).getBytes());
        }

        private static void fetch(UserPass up, Path path) throws IOException {
            XrayTest[] issues = fetchTestsByJQL("project=ANREIMAGED and type=Test and \"Cucumber Scenario\"~\"with attributes\"", up);
            LOG.info("Got issues: " + issues.length);
            IOUtil.mkdirs(path);
            for(XrayTest test : issues) {
                encodePretty(path.resolve(test.key + ".json"), test);
            }
        }

        public static List<XrayPreCondition> fetchAll(String projectKey, UserPass up) throws IOException {
            String jql = String.format("project = %s and type = Pre-Condition and labels = TestUser", projectKey);
            XrayPreCondition[] preConditions = JIRAUtil.fetchIssuesByJQL(jql, up, XrayPreConditionSearchResults.class);
            List<XrayPreCondition> precons = new ArrayList<>(preConditions.length);
            for(AbstractIssue issue : preConditions) {
                if(issue instanceof XrayPreCondition) {
                    precons.add((XrayPreCondition) issue);
                } else {
                    LOG.error("Unexpected non pre-condition: " + issue.getClass());
                }
            }
            return precons;
        }
    }

    static class AddTestsRequest extends AbstractRequest {
        enum IssueType {
            TestSet, TestPlan;

            String formatForRest() {
                return toString().toLowerCase();
            }
        }

        AddTestsRequest(String[] testKeys) {
            this.add = testKeys;
        }

        @JsonProperty
        public String[] add;

        Response add(IssueType type, String targetKey, String name, UserPass up) {
            Response response = post(JIRA_RAVEN_API_URL, toString(), format("%s/%s/test", type.formatForRest(), targetKey), up);
            return assertStatusCode(response, format("Add %s to %s", name, type.toString()), 200);
        }
    }

    public static class TestSet {
        public static Response addTest(String testKey, String testSetKey, UserPass up) throws IOException {
            return addTests(new String[] { testKey }, testSetKey, up);
        }

        public static Response addTests(String[] testKeys, String testSetKey, UserPass up) throws IOException {
            AddTestsRequest request = new AddTestsRequest(testKeys);
            return request.add(AddTestsRequest.IssueType.TestSet, testSetKey, "Tests", up);
        }

        public static Response deleteTest(String testKey, String testSetKey, UserPass up) throws IOException {
            String uri = String.format("%s/testset/%s/test/%s", JIRA_RAVEN_API_URL, testSetKey, testKey);
            RestAssuredResponseImpl response = RESTUtil.delete(uri, up);
            assertStatusCode(response, "Delete Test", 200);
            return response;
        }
    }

    public static class TestPlan {
        public static Response addTest(String testKey, String testPlanKey, UserPass up) throws IOException {
            return addTests(new String[] { testKey }, testPlanKey, up);
        }

        public static Response addTests(String[] testKeys, String testPlanKey, UserPass up) throws IOException {
            AddTestsRequest request = new AddTestsRequest(testKeys);
            return request.add(AddTestsRequest.IssueType.TestPlan, testPlanKey, "Test", up);
        }

        public static Response addTestSet(String testSetKey, String testPlanKey, UserPass up) throws IOException {
            AddTestsRequest request = new AddTestsRequest(XrayUtil.fetchTestKeysByTestSet(testSetKey, up.username, up.password));
            return request.add(AddTestsRequest.IssueType.TestPlan, testPlanKey, "Test Set", up);
        }

        public static Response deleteTest(String testKey, String testPlanKey, UserPass up) throws IOException {
            String uri = String.format("%s/testplan/%s/test/%s", JIRA_RAVEN_API_URL, testPlanKey, testKey);
            RestAssuredResponseImpl response = RESTUtil.delete(uri, up);
            assertStatusCode(response, "Delete Test", 200);
            return response;
        }
    }
}
