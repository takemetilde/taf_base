package com.taf.auto.jira.xray;

import com.taf.auto.IOUtil;
import com.taf.auto.common.PrettyPrinter;
import com.taf.auto.io.JSONUtil;
import com.taf.auto.jira.IssueHandle;
import com.taf.auto.jira.JIRAUtil;
import com.taf.auto.jira.pojo.User;
import com.taf.auto.jira.pojo.xray.XrayTest;
import com.taf.auto.rest.UserPass;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.taf.auto.IOUtil.NL;
import static com.taf.auto.StringUtil.splitNewlines;
import static com.taf.auto.jira.xray.XrayUtil.fetchTestsByJQL;

/**
 * Emits a fully formed scenario from an Xray Test Issue.
 *
 * @author AF04261 mmorton
 */
public class ScenarioEmitter {
    private static final Logger LOG = LoggerFactory.getLogger(ScenarioEmitter.class);

    private static final String INDENT = "  ";
    private static final String INDENT2 = INDENT + INDENT;
    private static final String TAG_PREFIX = "@";

    /**
     * POJO that holds the required information to build out a Scenario into a .feature file.
     */
    public static final class ScenarioDefinition {
        public final String key;
        public final String summary;
        public final String cucumberTestType;
        public final String cucumberScenario;

        public ScenarioDefinition(String key, String summary, String cucumberTestType, String cucumberScenario) {
            this.key = key;
            this.summary = summary;
            this.cucumberTestType = cucumberTestType;
            this.cucumberScenario = cucumberScenario;
        }

        public ScenarioDefinition(XrayTest test) {
            this(test.key, test.fields.summary, extractTestType(test), test.fields.cucumberScenario);
        }

        private static String extractTestType(XrayTest test) {
            if(null == test.fields.cucumberTestType) {
                throw new UnsupportedOperationException(format("Test %s is missing its Scenario Type", test.key));
            }
            return test.fields.cucumberTestType.value;
        }

        public ScenarioDefinition cloneWithScenario(String cucumberScenario) {
            return new ScenarioDefinition(key, summary, cucumberTestType, cucumberScenario);
        }
    }

    public static String emit(XrayTest test) {
        return emit(test, Collections.emptyList());
    }

    public static String emit(XrayTest test, String additionalTag) {
        return emit(test, Collections.singletonList(additionalTag));
    }

    public static String emit(XrayTest test, List<String> additionalTags) {
        return emit(new ScenarioDefinition(test), additionalTags);
    }

    public static String emit(ScenarioDefinition test, List<String> additionalTags) {
        StringBuilder out = new StringBuilder("Feature: ");
        out.append(test.summary).append(NL);

        List<String> tags = new ArrayList<>();
        tags.add(test.key);
        for (String tag : additionalTags) {
            if (!tags.contains(tag))
                tags.add(tag);
        }
        tags = ensureTagsStartWithAt(tags);
        
        out.append(INDENT).append(PrettyPrinter.prettyCollection(tags, " ")).append(NL);
        out.append(INDENT).append(test.cucumberTestType).append(": ").append(test.summary).append(NL);

        if(null != test.cucumberScenario) {
            String[] lines = splitNewlines(test.cucumberScenario);
            for(String line : lines){
                out.append(INDENT2).append(line).append(NL);
            }
        }
        String feature = out.toString();
        validate(feature);
        return feature;
    }

    /**
     * Parses the given Gherkin scenario to make sure it parses.
     *
     * @param feature a full feature file
     *
     * @throws MalformedFeatureException if feature does not parse or has no scenarios
     */
    public static void validate(String feature) throws MalformedFeatureException {
        List<Pickle> pickles;
        try {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            GherkinDocument gherkinDocument = parser.parse(feature);
            pickles = new Compiler().compile(gherkinDocument);
        } catch (Exception e) {
            LOG.error("Failed to validate Feature:\n" + feature, e);
            throw new MalformedFeatureException(e.getMessage());
        }
        if(pickles.isEmpty()) {
            throw new MalformedFeatureException("Feature does not contain any Scenarios");
        }
    }

    private static class MalformedScenarioScanner {

        private static void acquire(UserPass up) throws IOException {
            String jql = "project=ANREIMAGED and type=Test and \"Test Type\" = Cucumber and status != Closed";
            XrayTest[] tests = fetchTestsByJQL(jql, up);
            Path test_scan = getPathTests();
            IOUtil.mkdirs(test_scan);
            IOUtil.cleanDirectory(test_scan);
            for (XrayTest issue : tests) {
                JSONUtil.encode(test_scan.resolve(issue.key + ".json"), issue);
            }
        }

        private static Path getPathTests() {
            return XrayManifest.defineXrayPath().resolve("test_scan");
        }

        private static Path getPathTestResults() {
            return XrayManifest.defineXrayPath().resolve("test_scan_results.json");
        }

        private static Path getPathTestResultsUpdated() {
            return XrayManifest.defineXrayPath().resolve("test_scan_results_updated.json");
        }

        private static void validate() throws IOException {
            Map<String, String> map = new LinkedHashMap<>();
            Files.walk(getPathTests()).skip(1)
                    .forEach(f -> {
                        String key = f.getFileName().toString();
                        LOG.info("Validating: " + key);
                        try {
                            XrayTest test = JSONUtil.decode(f, XrayTest.class);
                            try {
                                emit(test);
                            } catch (MalformedFeatureException e) {
                                LOG.warn("Malformed: " + key);
                                map.put(key, e.getMessage());
                            }
                        } catch (IOException e) {
                            LOG.error("Failed to validate: " + key);
                        }
                    });
            JSONUtil.encodePretty(getPathTestResults(), map);
        }

        private static void update(UserPass up) throws IOException {
            Map<String, String> report = JSONUtil.decode(getPathTestResults(), Map.class);
            Path file = getPathTestResultsUpdated();
            Set<String> updated = Files.exists(file) ? JSONUtil.decode(file, Set.class) : new LinkedHashSet<>();

            Path pathTests = getPathTests();
            report.forEach((f, v) -> {
                Path guy = pathTests.resolve(f);
                XrayTest test;
                try {
                    test = JSONUtil.decode(guy, XrayTest.class);
                } catch (Exception e) {
                    LOG.error("Failed to decode: " + f, e);
                    return;
                }
                String key = test.key;
                if (!updated.contains(key)) {
                    LOG.info("Updating Test: " + key);
                    IssueHandle handle = new IssueHandle(key);
                    boolean success = true;
                    try {
                        JIRAUtil.Comment.add(handle, deriveComment(test, v), up);
                    } catch (Throwable t) {
                        LOG.error("Failed to add comment to: " + key, t);
                        success = false;
                    }
                    try {
                        JIRAUtil.Label.add(handle, Collections.singletonList("MalformedScenario"), up);
                    } catch (Throwable t) {
                        LOG.error("Failed to add label to: " + key, t);
                        success = false;
                    }
                    if (success) {
                        updated.add(key);
                        try {
                            JSONUtil.encode(file, updated);
                        } catch (IOException e) {
                            LOG.error("Failed to encoded updated memory", e);
                        }
                    }
                } else {
                    LOG.debug("Skipping already updated Test: " + key);
                }
            });
        }

        private static String deriveComment(XrayTest test, String reason) {
            String comment;
            if (reason.equals("Feature does not contain any Scenarios")) {
                comment = "The Scenario is missing or malformed. It may be a Scenario Outline without a valid Examples section.";
            } else if (reason.startsWith("Parser errors:")) {
                comment = "The Scenario could not be parsed. Verify Scenario vs Scenario Outline. Also look for stray characters or extra/missing data in examples.";
            } else {
                comment = reason;
            }

            StringBuilder msg = new StringBuilder();
            insertUser(msg, test.fields.assignee, null);
            insertUser(msg, test.fields.creator, test.fields.assignee);
            msg.append("This Test appears to be malformed, please investigate and repair.\n\nInfo: ");
            msg.append(comment);

            return msg.toString();
        }

        private static void insertUser(StringBuilder msg, User user, User otherUser) {
            if (null != user && user.active) {
                if (null == otherUser || !otherUser.name.equals(user.name)) {
                    msg.append("[~").append(user.name).append("] ");
                } else {
                    LOG.debug("Avoided implicating same user twice");
                }
            }
        }

        public static void main(String[] args) throws IOException {
            UserPass up = new UserPass("srcCONFLUENCEAPI", "");
            acquire(up);
            validate();
            update(up);
        }
    }

    public static List<String> ensureTagsStartWithAt(List<String> tags) {
        return tags.stream()
                .map(t -> t.startsWith(TAG_PREFIX) ? t : TAG_PREFIX + t)
                .collect(Collectors.toList());
    }
}
