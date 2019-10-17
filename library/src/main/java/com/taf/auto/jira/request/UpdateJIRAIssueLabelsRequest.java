package com.taf.auto.jira.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to update an existing issue with new tags.
 *
 */
public class UpdateJIRAIssueLabelsRequest extends AbstractRequest {
    @JsonProperty
    public Labels update;

    public UpdateJIRAIssueLabelsRequest(String[] labels) {
        update = new Labels(labels);
    }

    public static class Labels {
        @JsonProperty
        public AddLabel[] labels;

        Labels(String[] labels) {
            this.labels = new AddLabel[labels.length];
            for(int i = 0; i < labels.length; i++)
                this.labels[i] = new AddLabel(labels[i]);
        }
    }

    public static class AddLabel {
        @JsonProperty
        public String add;

        public AddLabel(String label) {
            this.add = label;
        }
    }
}
