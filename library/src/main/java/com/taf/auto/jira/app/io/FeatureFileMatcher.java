package com.taf.auto.jira.app.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/**
 * Matches any non directory that begins with the project name and ends in ".feature".
 *
 */
public final class FeatureFileMatcher implements PathMatcher {

    private final String project;

    public FeatureFileMatcher(String project) {
        this.project = project;
    }

    @Override
    public boolean matches(Path path) {
        String filename = path.getFileName().toString();
        return !Files.isDirectory(path) && filename.startsWith(project) && filename.endsWith(".feature");
    }
}
