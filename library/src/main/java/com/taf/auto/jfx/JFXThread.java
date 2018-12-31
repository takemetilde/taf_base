package com.taf.auto.jfx;

import com.taf.auto.common.RunSafe;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for managing JavaFX thread requirements.
 *
 */
public class JFXThread {
    private static final Logger LOG = LoggerFactory.getLogger(JFXThread.class);

    /**
     * Ensures the given logic executes in the JavaFX application thread ({@link Platform#isFxApplicationThread()}.
     * Calls {@link #jfxSafe(Runnable, boolean)} with the queue parameter set to {@code false}.
     * @param logic the logic to run
     */
    public static void jfxSafe(Runnable logic) {
        jfxSafe(logic, false);
    }

    /**
     * Ensures the given logic executes in the JavaFX application thread (see {@link Platform#isFxApplicationThread()}).
     * If called by a different thread, the logic will be queued via {@link Platform#runLater(Runnable)}. If called
     * by the JavaFX thread, the logic will either run in-line or be queued to {@link Platform#runLater(Runnable)}
     * based on the queue parameter. Queuing is useful if the logic should run after other operations have finished
     * (such as JavaFX layout).
     *
     * @param logic the logic to run
     * @param queue whether to queue if called by the JavaFX thread.
     */
    public static void jfxSafe(Runnable logic, boolean queue) {
        if(!queue && Platform.isFxApplicationThread()) {
            RunSafe.runSafe(logic);
        } else {
            LOG.trace("Queuing to JFX Thread");
            Platform.runLater(new RunSafe(() -> logic.run()));
        }
    }

    /**
     * Ensures the method does not return until the given logic executes on the JavaFX application thread
     * (see {@link Platform#isFxApplicationThread()}). If called by the JavaFX thread, the logic executes in-line.
     * Otherwise the calling thread is blocked until the logic completes.
     *
     * @param logic the logic to run
     */
    public static void jfxSafeWait(Runnable logic) {
        if(Platform.isFxApplicationThread()) {
            RunSafe.runSafe(logic);
        } else {
            LOG.trace("Queuing to JFX Thread and waiting");
            new JFXThreadLock().perform(logic);
        }
    }

    /**
     * Convenience method to throw a {@link RuntimeException} to enforce the calling method itself is
     * being called from the JavaFX application thread.
     *
     * @see Platform#isFxApplicationThread()
     */
    public static void throwIfNotJFXThread() {
        if(!Platform.isFxApplicationThread()) {
            throw new UnsupportedOperationException("This method must be called from the JavaFX application thread.");
        }
    }

    /**
     * Convenience method to throw a {@link RuntimeException} to enforce the calling method itself is
     * NOT being called from the JavaFX application thread.
     *
     * @see Platform#isFxApplicationThread()
     */
    public static void throwIfJFXThread() {
        if(Platform.isFxApplicationThread()) {
            throw new UnsupportedOperationException("This method must NOT be called from the JavaFX application thread.");
        }
    }
}
