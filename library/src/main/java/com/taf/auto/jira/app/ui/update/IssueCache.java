package com.taf.auto.jira.app.ui.update;

import com.taf.auto.jira.app.io.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Caches JIRA Issues locally.
 *
 */
public class IssueCache {
    private static final Logger LOG = LoggerFactory.getLogger(IssueCache.class);

    private static final SimpleCache cache = new SimpleCache("issue");

    public static Optional<byte[]> peek(String key) {
        return cache.peek(key, data -> Optional.of(data), () -> Optional.empty());
    }

    public static void poke(String key, byte[] data) {
        cache.poke(key, file -> {
            try {
                Files.write(file, data);
                LOG.info("Cached Issue: " + key);
            } catch (IOException e) {
                LOG.error("Failed to write cached Issue for:" + key, e);
            }
        });
    }

    public static void clear(String key) {
        cache.clear(key);
    }
}
