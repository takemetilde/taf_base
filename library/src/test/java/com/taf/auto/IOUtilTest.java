package com.taf.auto;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.taf.auto.IOUtil.cleanDirectory;
import static com.taf.auto.IOUtil.findNextAvailable;
import static org.junit.Assert.assertEquals;

/**
 * Unit Tests for {@link IOUtil}.
 *
 * @author AF04261
 */
public class IOUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(IOUtilTest.class);

    @Test
    public void findNextAvailableTest() throws IOException {
        final String ext = ".json";
        Path dir = Paths.get(Long.toString(System.currentTimeMillis()));
        // test a dir that shouldn't ever exit
        assertEquals(Optional.empty(), findNextAvailable(dir, ext));

        try {
            dir = Files.createTempDirectory("IOUtilTest_findNextAvailableTest");
            LOG.info("Created test dir: " + dir);

            // test first creation an empty dir
            Optional<Path> actual = findNextAvailable(dir, ext);
            assertEquals(Optional.of(dir.resolve("1" + ext)), actual);
            Files.createFile(actual.get());

            // create a file at the second slot
            Files.createFile(dir.resolve("2" + ext));
            // third slot should be returned
            assertEquals(Optional.of(dir.resolve("3" + ext)), findNextAvailable(dir, ext));
        } finally {
            LOG.debug("Cleaning up test dir");
            try {
                cleanDirectory(dir);
            } catch (Exception e) {
                LOG.error("Failed to delete temp dir", e);
            }
        }
    }
}
