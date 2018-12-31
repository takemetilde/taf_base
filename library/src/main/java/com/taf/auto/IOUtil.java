package com.taf.auto;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Library of IO related utility methods.
 *
 * @author AF04261 mmorton
 */
public final class IOUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IOUtil.class);

    /** Convenience access to <code>System.getProperty("line.separator")</code>. */
    public static final String NL = System.getProperty("line.separator");

    private IOUtil() { /** static only */ }

    /**
     * Closes the given {@link Closeable}, catching and logging any exception. If the given closeable is null this
     * method will noop.
     *
     * @param closeable the object to close.
     */
    public static void safeClose(Closeable closeable) {
        if(null != closeable) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                LOG.warn("Unable to close " + closeable, ioe);
            }
        }
    }

    /**
     * Creates all directories not present in the given path.
     *
     * @param path given path
     *
     * @throws IOException if unable to create any needed directories
     */
    public static void mkdirs(Path path) throws IOException {
        File file = path.toFile();
        if(!file.exists()) {
            boolean success = file.mkdirs();
            if (!success)
                throw new IOException("Failed to create all directories in path: " + path);
        }
    }

    /**
     * Deletes the contents within given directory without deleting the directory.
     *
     * @param path the dir to clean
     * @throws IOException if unable to delete any of the files or directories
     */
    public static void cleanDirectory(Path path) throws IOException {
        FileUtils.cleanDirectory(path.toFile());
    }

    /**
     * Generate a Path for a resource found on the classpath. This method does NOT work if running
     * from within a JAR. See {@link #readBytesFromClasspath(String)} to accomplish this.
     *
     * @param resourceName the given resource name
     * @return the path to this resource
     * @throws URISyntaxException if the given name does not resolve.
     */
    @Deprecated
    public static Path uriFromClasspath(String resourceName) throws URISyntaxException {
        if(resourceName.startsWith("/")) {
            LOG.debug("Stripping leading slash from: " + resourceName);
            resourceName = resourceName.substring(1);
        }
        URI uri = ClassLoader.getSystemResource(resourceName).toURI();
        return Paths.get(uri);
    }

    /**
     * Obtains the bytes from loading the specified resource. Will create a temporary {@link FileSystem} if
     * necessary.
     *
     * @param resourceName the given resource name (e.g. {@code /somefolder/about.png})
     * @return the raw bytes of the resource
     *
     * @throws IOException if problem accessing the resource
     * @throws URISyntaxException if the resourceName is invalid
     */
    public static byte[] readBytesFromClasspath(String resourceName) throws IOException, URISyntaxException {
        URL resource = IOUtil.class.getResource(resourceName);
        if(null == resource) {
            throw new IOException("System Resource not found in classpath: " + resourceName);
        }
        URI uri = resource.toURI();
        String[] array = uri.toString().split("!");
        Path path;
        FileSystem fs = null;
        if(array.length > 1) {
            LOG.debug("Creating temporary FileSystem to read URI: " + uri);
            fs = FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>());
            path = fs.getPath(array[1]);
        } else {
            path = Paths.get(uri);
        }
        try {
            return Files.readAllBytes(path);
        } finally {
            safeClose(fs);
        }
    }

    public static Optional<Path> findNextAvailable(Path dir, String extension) {
        return findNextAvailable(dir, extension, 1, 10000);
    }

    public static Optional<Path> findNextAvailable(Path dir, String extension, int startAt, int maxTries) {
        if(!Files.isDirectory(dir)) {
            LOG.error(format("Path: %s is not a valid directory", dir.toAbsolutePath()));
            return Optional.empty();
        }

        int index = startAt;
        for(int attempt = 1; attempt <= maxTries; attempt++, index++) {
            Path candidate = dir.resolve(format("%d%s", Integer.valueOf(index), extension));
            LOG.debug("Considering candidate: " + candidate);
            if(!Files.exists(candidate)) {
                LOG.info("Found available: " + candidate);
                return Optional.of(candidate);
            }
        }

        LOG.warn(format("Failed to find an available filename after %d tries", maxTries));
        return Optional.empty();
    }

    /**
     * Formats the given filename to make sure it does not contain reserved file system characters.
     *
     * @param filename the filename to encode
     * @return either an encoded version or the original if it did not require encoding
     */
    public static String formatLegalFilename(String filename) {
        String encoded = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        if(!filename.equals(encoded)) {
            LOG.info(format("Filename: %s encoded to: %s", filename, encoded));
            return encoded;
        }
        LOG.debug(format("Filename: %s did not require encoding", filename));
        return filename;
    }

    /**
     * Convenience method to return the working directory.
     *
     * @return the working dir
     */
    public static File peekWorkingDirectory() {
        return new File("workingdir").getAbsoluteFile().getParentFile();
    }
}
