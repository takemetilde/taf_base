package com.taf.auto.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Convenience code to wrap a {@link Runnable} in a try/catch.
 * A Runnable can be composed within an instance of this class or
 * passed directly into the static runSafe methods.
 *
 */
public final class RunSafe implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RunSafe.class);

    private Runnable logic;

    /**
     * Runs the Runnable inside the option if present, else no-op.
     *
     * @param optLogic the given logic option
     */
    public static void runSafe(@Nonnull Optional<Runnable> optLogic) {
        optLogic.ifPresent(r -> runSafe(r));
    }

    public static void runSafe(@Nonnull Runnable logic) {
        try {
            logic.run();
        }
        catch (Exception e) {
            LOG.error("Runnable threw: " + logic, e);
        }
    }

    public RunSafe(@Nonnull Runnable logic) {
        this.logic = logic;
    }

    @Override
    public final void run() {
        runSafe(logic);
    }

    /**
     * Runs the provided logic at least once. If the logic throws an exception
     * it will be run again until success or the number of retries is exceeded.
     * In this case the last failure will be rethrown.
     *
     * @param logic the given logic
     * @param numRetries how many times to retry
     */
    public static void runWithRetries(@Nonnull Runnable logic, int numRetries) {
        int tryNum = 1;
        RuntimeException lastException = null;
        while(tryNum <= numRetries) {
            if(tryNum > 1)
                LOG.info("Retrying...");
            try {
                logic.run();
                return;
            } catch (RuntimeException re) {
                LOG.warn("Try " + tryNum + " failed.", re);
                lastException = re;
            }
            tryNum++;
        }
        throw lastException;
    }
}
