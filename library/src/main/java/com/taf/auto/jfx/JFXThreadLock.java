package com.taf.auto.jfx;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.taf.auto.jfx.JFXThread.jfxSafe;

/**
 * Mechanism for blocking the calling thread until the given logic executes on the JavaFX thread.
 */
final class JFXThreadLock {
    private static final Logger LOG = LoggerFactory.getLogger(JFXThreadLock.class);

    private boolean resumed = false;

    void perform(Runnable logic) {
        if(Platform.isFxApplicationThread()) {
            throw new UnsupportedOperationException("Perform cannot be called from the JavaFX thread");
        }
        jfxSafe(() -> {
            try {
                logic.run();
            } catch (Throwable t) {
                LOG.error("Uncaught error in logic: " + logic, t);
            }
            resume();
        });
        stall();
    }

    private void stall() {
        synchronized (this) {
            if(resumed) {
               LOG.debug("Logic executed already, skipping wait");
               return;
            }
            try {
                LOG.debug("Waiting...");
                wait();
            } catch (InterruptedException e) {
                LOG.error("Wait interrupted", e);
            }
        }
    }

    private void resume() {
        synchronized (this) {
            LOG.debug("Resuming...");
            resumed = true;
            notifyAll();
        }
    }
}
