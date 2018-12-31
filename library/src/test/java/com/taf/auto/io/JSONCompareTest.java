package com.taf.auto.io;

import javafx.util.Pair;

import org.junit.Ignore;
import org.junit.Test;

import static com.taf.auto.common.PrettyPrinter.prettyList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.taf.auto.IOUtil.readBytesFromClasspath;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link JSONCompare}.
 */
@SuppressWarnings("restriction")
public class JSONCompareTest {

    private static String readJSON(String name) throws IOException, URISyntaxException {
        return new String(readBytesFromClasspath("/json/jsoncompare/" + name));
    }

    private static Pair<String, String> readJSONPair(String key) throws IOException, URISyntaxException {
        return new Pair<>(readJSON(key + "Actual.json"), readJSON(key + "Expected.json"));
    }

    /** Equivalent strings are technically supersets of each other */
    @Test
    public void supersetComparisonTest() {
        String json = "{ \"foo\": 1 }";
        List<String> strings = JSONCompare.supersetComparison(json, json);
        assertTrue(prettyList(strings), strings.isEmpty());
    }
    
    /**
     * Test the behavior of {@link JSONCompare#supersetComparison(String, String)} on a pair of example files. 
     * Give the file pair prefix and whether the tested method should allow them to pass as a superset relationship. 
     * 
     * @param filePrefix passed into {@link #readJSONPair(String)}
     * @param shouldPass give {@code true} if the two files represent an acceptable superset relationship, 
     * 		give {@code false} if the comparison algorithm should catch a problem in the example files 
     * @throws IOException
     * @throws URISyntaxException
     */
    private void testPair(String filePrefix, boolean shouldPass) throws IOException, URISyntaxException {
    		Pair<String, String> filePair = readJSONPair(filePrefix);
			List<String> strings = JSONCompare.supersetComparison(filePair.getKey(), filePair.getValue());
    		assertEquals(prettyList(strings), shouldPass, strings.isEmpty());
    }

    /** Equivalent strings are technically supersets of each other */
    @Test
    public void trivialSupersetComparisonTest() throws IOException, URISyntaxException {
        testPair("trivial", true);
    }
    
    /** Allow ordering adjustments */
    @Test
    public void orderingSupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("ordering", true);
    }
    
    /** Allow additions */
    @Test
    public void additionsSupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("additions", true);
    }
    
    /** A superset cannot remove elements */
    @Test
    public void omissionsSupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("omissions", false);
    }
    
    /** A superset cannot change elements' keys (reduces to removal) */
    @Test public void keyChangeSupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("keyChange", false);
    }
    
    /** A superset cannot change elements' values */
    @Test
    public void valueChangeSupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("valueChange", false);
    }
    
    /** A superset cannot move elements between hierarchy levels (reduces to removal) */
    @Test
    public void hierarchySupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("hierarchy", false);
    }
    
    /** Matching objects inside arrays */
    @Test
    public void deepArraySupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("deepArray", true);
    }
    
    /** Allow a single layer of wrapping */
    @Test
    public void nestedSupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("nested", true);
    }
    
    /** Allow infinite layers of wrapping */
    @Test
    public void multipleNestingSupersetComparisonTest()  throws IOException, URISyntaxException {
    	testPair("multipleNesting", true);
    }
    
    /** Allow a object's key nested in a container, not an immediate child of its original parent */
    @Test
    public void deepKeySupersetComparisonTest() throws IOException, URISyntaxException {
    	testPair("deepKey", true);
    }
}
