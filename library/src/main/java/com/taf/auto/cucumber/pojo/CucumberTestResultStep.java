package com.taf.auto.cucumber.pojo;

import com.taf.auto.json.SparseJsonPojo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CucumberTestResultStep extends SparseJsonPojo {
    @JsonProperty
    public CucumberStepResult result;

    @JsonProperty
    public int line;

    @JsonProperty
    public String name;

    @JsonProperty
    public CucumberStepMatch match;

    @JsonIgnore
    public String rows;

    @JsonProperty
    public String keyword;

    @JsonProperty
    public CucumberStepEmbeddings[] embeddings;

    @JsonProperty
    public String[] output;
    
    public void appendEmbeddings(CucumberStepEmbeddings... embeddingsToAppend) {
        if (embeddings == null) {
            embeddings = embeddingsToAppend.clone();
            return;
        }
        
        CucumberStepEmbeddings[] combined = new CucumberStepEmbeddings[embeddings.length + embeddingsToAppend.length];
        System.arraycopy(embeddings, 0, combined, 0, embeddings.length);;
        System.arraycopy(embeddingsToAppend, 0, combined, embeddings.length, embeddingsToAppend.length);
        embeddings = combined;
    }
}
