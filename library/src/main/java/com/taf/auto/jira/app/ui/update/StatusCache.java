package com.taf.auto.jira.app.ui.update;

import com.taf.auto.io.JSONUtil;
import com.taf.auto.jira.app.io.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class StatusCache {
    private static final Logger LOG = LoggerFactory.getLogger(StatusCache.class);

    private static final SimpleCache cache = new SimpleCache("status");

    public static IssueUpdateStatus peek(String key) {
        return cache.peek(key, StatusCache::adapt, () -> IssueUpdateStatus.Pending);
    }

    private static IssueUpdateStatus adapt(byte[] data) {
        try {
            return JSONUtil.decode(data, IssueUpdateStatus.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode", e);
        }
    }

    public static void poke(String key, IssueUpdateStatus value) {
        cache.poke(key, file -> {
            try {
                JSONUtil.encode(file, value);
                LOG.info("Cached Status: " + key + " value: " + value);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write cached Issue for:" + key, e);
            }
        });
    }
    public static void main(String[] args) {
        Path target = Paths.get("C:\\Users\\AF04261\\IdeaProjects\\memberportal-testautomation\\src\\test\\resources\\com\\anthem\\portal");

        class Viz extends SimpleFileVisitor<Path> {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("Processing: " + file);
                String data = new String(Files.readAllBytes(file));
                String name = file.getFileName().toString();
                String realName = name.substring(0, name.indexOf(".json")) + ".feature";
                if("\"Success\"".equals(data)) {
                    Files.find(target, Integer.MAX_VALUE, (t, u) -> {
                        return t.endsWith(realName);
                    })
                            .forEach(f -> {
                                try {
                                    System.out.println("Deleting: " + f);
                                    Files.delete(f);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                }
                return FileVisitResult.CONTINUE;
            }
        }

        try {
            Files.walkFileTree(Paths.get("cache/status"), new Viz());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
