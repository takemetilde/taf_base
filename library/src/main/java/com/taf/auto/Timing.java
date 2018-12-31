package com.taf.auto;

import org.slf4j.Logger;

/**
 * Utility class for measuring runtimes.
 *
 * @author mmorton AF04261
 */
public final class Timing {

    private long referenceTime;

    public Timing() {
        reset();
    }

    public void reset() {
        referenceTime = System.currentTimeMillis();
    }

    public String report(String preamble) {
        long delta = System.currentTimeMillis() - referenceTime;
        StringBuilder msg = new StringBuilder("--TIMING--> ");
        msg.append(preamble).append(" in ").append(delta).append("ms");
        return msg.toString();
    }

    public void report(String preamble, Logger log) {
        log.trace(report(preamble));
    }
}
