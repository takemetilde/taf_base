package com.taf.auto.jira.app.ui.update;

import java.nio.file.Path;

/**
 * POJO for data when a feature file has an error.
 */
public final class CollectFeatureFileError {
    public final String key;
    public final Path file;
    public final String msg;

    public CollectFeatureFileError(String key, Path file, String msg) {
        this.key = key;
        this.file = file;
        this.msg = msg;
    }
}
