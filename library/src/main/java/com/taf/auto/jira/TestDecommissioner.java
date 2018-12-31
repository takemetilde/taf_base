package com.taf.auto.jira;

import com.taf.auto.io.JSONUtil;
import com.taf.auto.jira.pojo.IssueLink;
import com.taf.auto.jira.pojo.xray.XrayTest;
import com.taf.auto.jira.xray.XrayManifest;
import com.taf.auto.jira.xray.XrayUtil;
import com.taf.auto.jira.xray.pojo.XrayTestRun;
import com.taf.auto.rest.UserPass;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.taf.auto.io.JSONUtil.decode;
import static com.taf.auto.io.JSONUtil.encodePretty;
import static com.taf.auto.jira.JIRAUtil.isIssueClosed;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

/**
 * Scans Xray Tests to detect which should be decommissioned.
 *
 * @author AF04261 mmorton
 */
final class TestDecommissioner {
    private static final Logger LOG = LoggerFactory.getLogger(TestDecommissioner.class);

    enum Status {
        Hit,
        Strike1,
        Strike2,
        Strike3;

        static Status degrade(Status s) {
            switch (s) {
                case Hit: return Status.Strike1;
                case Strike1: return Status.Strike2;
                case Strike2: return Status.Strike3;
                default: throw new RuntimeException("Unexpected status: " + s);
            }
        }
    }

    static Path path(String subpath) {
        return XrayManifest.defineXraySubpath("decom/" + subpath);
    }

    static Path pathTestPlan() {
        return path("testplan");
    }

    static Path pathTest() {
        return path("test");
    }

    static Path pathTestRuns() {
        return path("testruns");
    }

    static Path pathReckoningLedger() {
        return XrayManifest.defineXraySubpath("decom").resolve("ledger.json");
    }

    static Path pathHammered() {
        return path("hammered");
    }

    static boolean shouldDecommission(XrayTestRun[] testRuns) {
        if(testRuns.length < 1) {
            return false;
        }

        LOG.info("Determining decommission status for Test: {}", testRuns[0].testKey);

        Set<String> environments = detectEnvironments(testRuns);

        Map<String, Status> counts = new LinkedHashMap<>();

        // start at the end of the array to process the newest test runs first
        for(int i = testRuns.length; i-->0;) {
            if(reckonTestRun(testRuns[i], counts, environments)) {
                LOG.info("Decommission: " + true);
                return true;
            }
        }

        LOG.info("Decommission: " + false);
        return false;
    }

    static boolean naEnv(String env) {
        String[] relevant = { "DEV2", "SIT1"};
        for(String e : relevant) {
            if(e.equals(env)) {
                return false;
            }
        }
        return true;
    }

    static Set<String> detectEnvironments(XrayTestRun[] testRuns) {
        Set<String> envs = new LinkedHashSet<>();
        for(XrayTestRun run : testRuns) {
            for(String env : run.testEnvironments) {
                if(naEnv(env))
                    continue;
                boolean add = envs.add(env);
                if(add) {
                    LOG.debug("Detected environment: " + env);
                }
            }
        }
        return envs;
    }

    private static boolean reckonTestRun(XrayTestRun testRun, Map<String, Status> memory, Set<String> environments) {
        LOG.debug("  Reckoning Test Run for Test Execution: {}", testRun.testExecKey);
        for (String env : testRun.testEnvironments) {
            if(naEnv(env))
                continue;
            Status count = memory.get(env);
            if(count == Status.Hit || count == Status.Strike3) {
                continue;
            }

            Status next;
            if("PASS".equals(testRun.status)) {
                next = Status.Hit;
            } else if(null == count) {
                next = Status.Strike1;
            } else {
                next = Status.degrade(count);
            }
            LOG.info("  {}: {}", env, next);
            memory.put(env, next);
        }

        return shouldDecommission(memory, environments);
    }

    private static boolean shouldDecommission(Map<String, Status> memory, Set<String> environments) {
        if(memory.isEmpty()) {
            return false;
        }

        Set<String> envsEncountered = memory.keySet();
        for(String envDetected : environments) {
            if(!envsEncountered.contains(envDetected)) {
                // not all environments have been detected yet
                return false;
            }
        }

        for(Map.Entry<String, Status> entry : memory.entrySet()) {
            if(entry.getValue() != Status.Strike3) {
                return false;
            }
        }
        return true;
    }

    /**
     * For each Regression test (DEV and SIT) collect all Xray Tests, excluding a Test created within the last month.
     */
    public static class Acquisition {
        public static void acquireAll(UserPass up, String testPlanKey) throws IOException {
            IssueTypes type = JIRAUtil.fetchIssueType(testPlanKey, up);
            assertEquals(IssueTypes.Test_Plan, type);

            XrayTest[] tests = XrayUtil.fetchTestsByTestPlan(testPlanKey, up.username, up.password);
            LOG.info("Acquired {} Tests from Test Plan: {}", tests.length, testPlanKey);

            for(XrayTest test : tests) {
                if(naTest(test)) {
                    LOG.warn("Test: {} is too new ({}) and will not be considered.", test.key, test.fields.created);
                    continue;
                }
                encodePretty(pathTest().resolve(test.key + ".json"), test);
            }

            for(XrayTest test : tests) {
                if(naTest(test)) {
                    continue;
                }
                XrayTestRun[] testRuns = XrayUtil.Test.getTestRuns(test.key, up);
                LOG.info("Acquired {} Test Runs for Test: {}", testRuns.length, test.key);
                encodePretty(pathTestRuns().resolve(test.key + ".json"), testRuns);
            }
        }

        private static boolean naTest(XrayTest test) {
            LocalDate created = JIRAUtil.parseDateTime(test.fields.created);
            LocalDate today = LocalDate.now();
            LocalDate monthAgo = today.minusDays(31);
            return created.isAfter(monthAgo);
        }

        public static void main(String[] args) throws IOException {
            String plans = "ANREIMAGED-34112, ANREIMAGED-25991, ANREIMAGED-25987, ANREIMAGED-25985, ANREIMAGED-25986, " +
                    "ANREIMAGED-25990, ANREIMAGED-25988, ANREIMAGED-25989, ANREIMAGED-25983, ANREIMAGED-25984";
            UserPass up = UserPass.glean();
            for(String planKey : plans.split(",")) {
                acquireAll(up, planKey.trim());
            }
        }
    }

    public static class ReckoningLedger {
        @JsonProperty
        public List<String> testsToDecom = new ArrayList<>();
    }

    private static String loseExt(Path file) {
        return FilenameUtils.getBaseName(file.getFileName().toString());
    }

    /**
     * For each Xray Test, download all the Test Runs and if the last 3 results are FAILED in ALL implicated
     * environments* then <b>note</b> the Test.
     * <p>
     * <i>*only DEV2 and SIT1 are considered relevant. Any test runs for a different environment will be ignored.</i>
     */
    public static class Reckoning {
        public static void main(String[] args) throws IOException {
            ReckoningLedger ledger = new ReckoningLedger();

            AtomicInteger count = new AtomicInteger();
            Files.walk(pathTestRuns()).skip(1).forEach(f -> {
                count.incrementAndGet();
                XrayTestRun[] testRuns;
                try {
                    testRuns = JSONUtil.decode(f, XrayTestRun[].class);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to decode: " + f);
                }
                boolean shouldDecommission = shouldDecommission(testRuns);
                if(shouldDecommission) {
                    String testKey = loseExt(f);
                    ledger.testsToDecom.add(testKey);
                }
            });

            LOG.info("Marked {} of {} Tests for decommission.", ledger.testsToDecom.size(), count.get());

            encodePretty(pathReckoningLedger(), ledger);
        }
    }

    /**
     * For each noted Test:
     * <ol>
     * <li> Remove any "Tests" link to User Stories.
     * <li> Add "Related to" link to User Story to preserve the vestigial connection
     * <li> Add "decommissioned" label
     * <li> Set status to Closed
     * </ol>
     */
    public static class TheHammer {
        private static void drop(XrayTest test, UserPass up) throws IOException {
            LOG.info("Dropping TheHammer on Test: {}", test.key);

            if(isIssueClosed(test)) {
                JIRAUtil.Status.reopenClosed(test.key, up);
            }

            StringBuilder comment = new StringBuilder("This test is decommissioned.\n\n");

            for(IssueLink link : test.fields.issuelinks) {
                if(null == link.outwardIssue) {
                    continue;
                }
                LOG.debug("  Inspecting link: {} to: {}", link.type.name, link.outwardIssue.key);
                if(IssueLinkNames.Tests.matches(link.type.name)) {
                    LOG.info("Noting {} coverage: {}", IssueLinkNames.Tests, link.outwardIssue.key);
                    try {
                        JIRAUtil.IssueLink.delete(link.id, up);
                        comment.append("\nUsed to test ").append(link.outwardIssue.key);
                    } catch (Exception e) {
                        String msg = format("Failed to delete link to: {} probably because it is CLOSED", link.outwardIssue.key);
                        LOG.error(msg);
                        comment.append("\n" + msg);
                    }
                }
            }

            for(String testPlanKey : test.fields.testPlanKeys) {
                LOG.info("  Noting membership in Test Plan: {}", testPlanKey);
                try {
                    XrayUtil.TestPlan.deleteTest(test.key, testPlanKey, up);
                    comment.append("\nUsed to be in Test Plan: " + testPlanKey);
                } catch (Exception e) {
                    String msg = format("Failed to remove from Test Plan: %s", testPlanKey);
                    LOG.error(msg);
                    comment.append("\n" + msg);
                }
            }

            for(String testSetKey : test.fields.testSetKeys) {
                LOG.info("  Noting membership in Test Set: {}", testSetKey);
                try {
                    XrayUtil.TestSet.deleteTest(test.key, testSetKey, up);
                    comment.append("\nUsed to be in Test Set: " + testSetKey);
                } catch (Exception e) {
                    String msg = format("Failed to remove from Test Set: %s", testSetKey);
                    LOG.error(msg);
                    comment.append("\n" + msg);
                }
            }

            IssueHandle handle = new IssueHandle(test.key);
            JIRAUtil.Label.add(handle, Collections.singletonList("decommissioned"), up);

            JIRAUtil.Comment.add(handle, comment.toString(), up);

            Optional<String> optAssigneeName = null == test.fields.assignee || test.fields.assignee.active ? Optional.empty() : Optional.of("");
            JIRAUtil.Status.close(test.key, "Abandoned", optAssigneeName, up);
        }

        private static boolean alreadyHammered(String testKey) {
            return Files.exists(pathHammered().resolve(testKey));
        }

        private static void record(String testKey) {
            try {
                Files.createFile(pathHammered().resolve(testKey));
            } catch (IOException e) {
                LOG.error("Failed to record hammering of Test: {}", testKey);
            }
        }

        public static void main(String[] args) throws IOException {
            UserPass up = UserPass.glean();

            ReckoningLedger ledger = decode(pathReckoningLedger(), ReckoningLedger.class);
            Set<String> validTargets = new HashSet<>(ledger.testsToDecom);

            Files.walk(pathTest()).skip(1).forEach(f -> {
                String testKey = loseExt(f);

                if(alreadyHammered(testKey)) {
                    LOG.warn("Ignoring already hammered Test: {}", testKey);
                    return;
                }

                if(validTargets.contains(testKey)) {
                    try {
                        drop(decode(f, XrayTest.class), up);
                        record(testKey);
                    } catch (Exception e) {
                        LOG.error("Failed to drop TheHammer on Test: {}", testKey, e);
                    }
                } else {
                    LOG.debug("Ignoring: {}", testKey);
                }
            });
        }
    }
}
