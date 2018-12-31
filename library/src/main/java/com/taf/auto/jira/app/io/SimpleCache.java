package com.taf.auto.jira.app.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * For caching files within a given subdir of ./cache.
 *
 */
public class SimpleCache {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCache.class);

    private final String subfolder;

    public SimpleCache(@Nonnull String subfolder) {
        this.subfolder = subfolder;
    }

    private static Path peekCacheFolder() throws IOException {
        Path path = Paths.get("cache");
        if(!Files.isDirectory(path)) {
            Files.createDirectory(path);
        }
        return path;
    }

    public final Path peekSubfolder() throws IOException {
        Path path = peekCacheFolder().resolve(subfolder);
        if(!Files.isDirectory(path)) {
            Files.createDirectory(path);
        }
        return path;
    }

    protected Path peekPath(String key) throws IOException {
        return peekSubfolder().resolve(key + "." + defineExtension());
    }

    protected String defineExtension() {
        return "json";
    }

    public <T> T peek(String key, Function<byte[], T> adapter, Supplier<T> fallback) {
        Path file;
        try {
            file = peekPath(key);
        } catch (IOException e) {
            LOG.error("Failed to generate path for: " + key, e);
            return fallback.get();
        }

        if(Files.exists(file)) {
            try {
                byte[] data = Files.readAllBytes(file);
                return adapter.apply(data);
            } catch (Exception e) {
                LOG.error("Failed to read cached " + subfolder + " from: " + file.toAbsolutePath(), e);
            }
        } else {
            LOG.info("No cache for " + subfolder + ": " + key);
        }

        return fallback.get();
    }

    public void poke(String key, Consumer<Path> writer) {
        try {
            Path file = peekPath(key);
            writer.accept(file);
        } catch (Exception e) {
            LOG.error("Uncaught exception poking:" + key, e);
        }
    }

    public void clear(String key) {
        try {
            Path file = peekPath(key);
            if(Files.exists(file)) {
                boolean deleted = Files.deleteIfExists(file);
                if (!deleted)
                    LOG.error("Failed to delete:" + key + " file: " + file);
            }
        } catch (Exception e) {
            LOG.error("Uncaught exception clearing:" + key, e);
        }
    }
}
