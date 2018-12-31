package com.taf.auto.jira.app.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FeatureFileVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureFileVisitor.class);

    private final FeatureFileMatcher matcher;

    private List<Path> files;

    public FeatureFileVisitor(String project) {
        this.files = new ArrayList<>();
        if(null == project || project.isEmpty())
            throw new IllegalArgumentException("project key must be specified");
        matcher = new FeatureFileMatcher(project);
    }

    public List<Path> getFiles() {
        return files;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(matcher.matches(file)) {
            LOG.debug("Found match: "+ file);
            files.add(file);
        }
        return super.visitFile(file, attrs);
    }

    public static List<Path> collectScenarios(String project, Path dir) throws IOException {
        FeatureFileVisitor visitor = new FeatureFileVisitor(project);
        Files.walkFileTree(dir, visitor);
        return visitor.getFiles();
    }
}
