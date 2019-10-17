package com.taf.auto.jira.xray.pojo;

import com.taf.auto.jira.pojo.xray.XrayTest;
import com.taf.auto.jira.request.AbstractRequest;
import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO imitating Cucumber's JSON test format for purposes of creating a Test Execution with.
 *
 */
public class CucumberTestResult extends AbstractRequest {
    @JsonProperty
    public int line;

    @JsonProperty
    public Element[] elements;

    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    public String id;

    @JsonProperty
    public String keyword;

    @JsonProperty
    public String uri;

    @JsonProperty
    public Tag[] tags;

    public static class Element {
        @JsonProperty
        public int line;

        @JsonProperty
        public ResultWithMatch[] before;

        @JsonProperty
        public String name;

        @JsonProperty
        public String description;

        @JsonProperty
        public String id;

        @JsonProperty
        public ResultWithMatch[] after;

        @JsonProperty
        public String type;

        @JsonProperty
        public String keyword;

        @JsonProperty
        public Step[] steps;

        @JsonProperty
        public Tag[] tags;
    }

    public static class Step {
        @JsonProperty
        public Result result;

        @JsonProperty
        public int line;

        @JsonProperty
        public String name;

        @JsonProperty
        public Match match;

        @JsonProperty
        public int[] matchedColumns;

        @JsonProperty
        public Row[] rows;

        @JsonProperty
        public String keyword;

        @JsonProperty
        public Embedding[] embeddings;
        
        @JsonProperty
        public String[] output;
    }

    public static class Row {
        @JsonProperty
        public String[] cells;

        @JsonProperty
        public int line;
    }

    public static class Result {
        @JsonProperty
        public long duration;

        @JsonProperty
        public String error_message;

        @JsonProperty
        public String status;

        public Result() { }

        public Result(String status) {
            duration = 1;
            this.status = status;
        }

        public Result(String status, String error_message) {
            this(status);
            this.error_message = error_message;
        }
    }

    public static class Embedding extends SparseJsonPojo {
        @JsonProperty
        public String data;

        @JsonProperty
        public String mime_type;
    }

    public static class Match {
        @JsonProperty
        public Argument[] arguments;

        @JsonProperty
        public String location;
    }

    public static class Argument {
        @JsonProperty
        public String val;

        @JsonProperty
        public int offset ;
    }

    public static class ResultWithMatch {
        @JsonProperty
        public Result result;

        @JsonProperty
        public Match match;
    }

    public static class Tag {
        @JsonProperty
        public int line;

        @JsonProperty
        public String name;

        public Tag() {
        }

        public Tag(String name) {
            this.line = 2;
            this.name = name;
        }
    }

    /**
     * Build out a result from the given {@link XrayTest} and desired status.
     *
     * @param test the test to resolve from
     * @param status the status to use
     * @param message the message to include
     * @return the corresponding result
     */
    public static CucumberTestResult resolve(XrayTest test, String status, String message) {
        CucumberTestResult ctr = new CucumberTestResult();
        ctr.line = 1;
        ctr.name = test.fields.summary;
        ctr.description = "";
        ctr.id = ctr.name.toLowerCase().replace(" ", "-");
        ctr.keyword = "Feature";
        ctr.uri = test.key;
        ctr.elements = resolveElements(test, ctr, status, message);
        return ctr;
    }

    private static Element[] resolveElements(XrayTest test, CucumberTestResult ctr, String status, String message) {
        Element e = new Element();
        // before
        e.line = 1;
        e.name = ctr.name;
        e.description = ctr.description;
        e.id = ctr.id;
        // after
        e.type = "scenario";
        e.keyword = null != test.fields.cucumberTestType ? test.fields.cucumberTestType.value : "Scenario";
        e.steps = resolveSteps(status, message);
        e.tags = new Tag[] { new Tag("@" + test.key) };
        return new Element[] { e };
    }

    private static Step[] resolveSteps(String status, String message) {
        Step step = new Step();
        step.result = new Result(status, message);
        step.line = 3;
        return new Step[] { step };
    }
}

