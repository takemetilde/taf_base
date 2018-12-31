package com.taf.auto.cucumber;

import com.taf.auto.cucumber.pojo.*;
import com.taf.auto.cucumber.pojo.CucumberTestResult;
import com.taf.auto.io.JSONUtil;
import com.taf.auto.jira.xray.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.taf.auto.io.JSONUtil.decode;
import static com.taf.auto.io.JSONUtil.encodePretty;
import static com.taf.auto.jira.xray.pojo.XrayExecutionTest.Status.*;

/**
 * Utility methods for working with Cucumber reports.
 */
public class CucumberUtil {
    
    private static final Logger LOG = LoggerFactory.getLogger(CucumberUtil.class);
    
    /**
     * Scan the given directory to return all files that have a filename that parses to an integer
     * and ends with the given extension.
     *
     * @param dir the directory to scan
     * @param extension the file extension to use
     *
     * @return the matching files
     */
    public static List<File> collectNumberedFiles(File dir, String extension) {
        FilenameFilter filter = (d, name) -> name.toLowerCase().matches("[0-9]+\\." + extension);
        return Arrays.asList(dir.listFiles(filter));
    }
    
    public static XrayExecutionResult mergeTestResultsToXray(List<File> jsonFilesToMerge) throws IOException {
        XrayExecutionResult xer = new XrayExecutionResult();
        xer.tests = new XrayExecutionTest[jsonFilesToMerge.size()];
        int i = 0;
        for (File f : jsonFilesToMerge) {
            try {
                CucumberTestResult[] result = JSONUtil.decode(f, CucumberTestResult[].class);
                if (result.length != 1) {
                    throw new Exception("Expected a single result but got: " + result.length);
                }
                
                xer.tests[i++] = extractCucumberToXray(result[0]);
            } catch (Exception e) {
                throw new IOException("Failed to decode: " + f, e);
            }
        }
        
        xer.info = new XrayExecutionResultInfo();
        xer.info.summary = "Testing New Xray Push";
        xer.info.description = "Test Description";
        
        return xer;
    }
    
    static XrayExecutionTest extractCucumberToXray(CucumberTestResult in) {
        XrayExecutionTest test = new XrayExecutionTest();
        test.testKey = deriveXrayTestKeyFromURI(in.uri);
        
        if (in.elements.length != 1) {
            throw new IllegalArgumentException("Expected a single element but got: " + in.elements.length);
        }
        CucumberTestResultStep[] inSteps = in.elements[0].steps;
        test.steps = new XrayExecutionStep[inSteps.length];
        
        // scan the steps to record them plus the aggregate status
        for (int i = 0; i < inSteps.length; i++) {
            test.steps[i] = resolveStep(inSteps[i]);
        }
        test.status = resolveStatus(test.steps);
        
        return test;
    }
    
    private static XrayExecutionStep resolveStep(CucumberTestResultStep in) {
        XrayExecutionStep step = new XrayExecutionStep();
        step.status = mapStatusToXray(in.result.status);
        if (FAIL.equals(step.status)) {
            step.comment = Pattern.compile("\\\\").matcher(in.result.error_message).replaceAll("\\\\\\\\");
        }
        return step;
    }
    
    private static String resolveStatus(XrayExecutionStep[] steps) {
        for (XrayExecutionStep step : steps) {
            switch (step.status) {
                case FAIL:
                    return FAIL;
                case ABORTED:
                    return ABORTED;
            }
        }
        return PASS;
    }
    
    private static String mapStatusToXray(String cucumberStatus) {
        switch (cucumberStatus) {
            case "passed":
                return PASS;
            case "failed":
                return FAIL;
            case "skipped":
                return ABORTED;
            default:
                LOG.error("Unknown cucumberStatus: " + cucumberStatus);
                return FAIL;
        }
    }
    
    static String deriveXrayTestKeyFromURI(String uri) {
        int at = uri.lastIndexOf("/");
        if (-1 == at || at == uri.length() - 1) {
            throw new IllegalArgumentException("URI does not match expected pattern: " + uri);
        }
        String key = uri.substring(at + 1);
        String suffix = ".feature";
        if (key.endsWith(suffix)) {
            key = key.substring(0, key.length() - suffix.length());
        }
        return key;
    }
    
    static Optional<String> extractMergeKey(String uri) {
        if (null == uri) {
            return Optional.empty();
        }
        
        int at = uri.lastIndexOf('/');
        if (-1 == at || at == uri.length() - 1) {
            return Optional.empty();
        }
        
        String possibleKey = uri.substring(at + 1);
        if (!possibleKey.endsWith(").feature")) {
            return Optional.empty();
        }
        
        at = possibleKey.indexOf('(');
        if (-1 == at) {
            return Optional.empty();
        }
        
        return Optional.of(possibleKey.substring(0, at));
    }
    
    public static void mergeCucumberReports(File mergedJsonFile, List<File> jsonFilesToMerge) throws IOException {
        LOG.info("Merging " + jsonFilesToMerge.size() + " JSON file(s) to: " + mergedJsonFile);
        
        List<CucumberTestResult> mergedResults = new ArrayList<>();
        
        Map<String, CucumberTestResult> toMerge = new HashMap<>();
        
        for (File jsonFile : jsonFilesToMerge) {
            LOG.info("Merging: " + jsonFile);
            if (!jsonFile.isFile()) {
                LOG.error("Not a valid file: " + jsonFile);
                continue;
            }
            
            CucumberTestResult[] results = decode(jsonFile, CucumberTestResult[].class);
            for (CucumberTestResult result : results) {
                embedOutput(result);

                Optional<String> optMergeKey = extractMergeKey(result.uri);
                if (optMergeKey.isPresent()) {
                    String key = optMergeKey.get();
                    CucumberTestResult existingResult = toMerge.get(key);
                    if (null == existingResult) {
                        LOG.debug("First mergeable result for: " + key);
                        mergedResults.add(result);
                    }
                    else {
                        LOG.debug("Subsequent mergeable result for: " + key);
                        existingResult.appendElement(result.elements);
                    }
                }
                else {
                    mergedResults.add(result);
                }
            }
        }
        
        encodePretty(mergedJsonFile, mergedResults.toArray(new CucumberTestResult[mergedResults.size()]));
    }
    
    /**
     * Creates and adds an "embedding" entry for each step result that has one or more "output" entries.
     * <p>
     * This will result in the output for each step being attached to the step results as "evidence" in JIRA .
     *
     * @param result test result object
     */
    private static void embedOutput(CucumberTestResult result) {
        for (CucumberTestResultElement resultElement : result.elements) {
            for (CucumberTestResultStep step : resultElement.steps) {
                if (step.output == null || step.output.length == 0) {
                    continue;
                }
                
                CucumberStepEmbeddings embedding = new CucumberStepEmbeddings();
                embedding.data = encodeBase64(String.join("\n", step.output));
                embedding.mime_type = "text/plain";
                
                step.appendEmbeddings(embedding);
            }
        }
    }
    
    private static String encodeBase64(String text) {
        return new String(Base64.getEncoder().encode(text.getBytes()));
    }
}
