package com.taf.auto.jfx.app.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple logger for capturing activity.
 */
public class UIActivityLog {
    private static final Logger LOG = LoggerFactory.getLogger(UIActivityLog.class);

    private static final String NL = System.getProperty("line.separator");

    private final Path logFile;
    private final SimpleDateFormat dateFormat;

    public UIActivityLog(String name) {
        File file = new File(name + ".log");
        if(!file.exists())
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                LOG.error("Failed to create log file: " + file, ioe);
            }
        logFile = file.toPath();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    }

    public synchronized void log(String activity) {
        StringBuilder msg = new StringBuilder("[[[ ACTIVITY ]]] ");
        msg.append(dateFormat.format(new Date())).append(" - ").append(activity).append(NL);

        String s = msg.toString();
        LOG.info(s);
        try {
            Files.write(logFile, s.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ioe) {
            LOG.error("Failed to log activity: " + activity, ioe);
        }
    }

    public static void main(String[] args) {
        UIActivityLog test = new UIActivityLog("Test");
        test.log("hello");
    }
}
