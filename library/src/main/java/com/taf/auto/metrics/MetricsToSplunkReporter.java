package com.taf.auto.metrics;

import com.taf.auto.common.Configuration;
import cucumber.api.Scenario;
import cucumber.runtime.ScenarioImpl;
import gherkin.formatter.model.Result;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.taf.auto.common.Configuration.getComputerName;
import static com.taf.auto.common.PrettyPrinter.prettyCollection;

/**
 * Pushes key metrics from a {@link Scenario} to a MySQL database.
 *
 */
public class MetricsToSplunkReporter {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsToSplunkReporter.class);

    private static class SplunkDestination implements Consumer<String> {
        private final String filename;

        SplunkDestination(String filename) {
            this.filename = filename;
        }

        @Override
        public void accept(String s) {
            try {
                LOG.info("METRICS: " + s);
                String out = s + Configuration.NEW_LINE;
                Files.write(Paths.get(filename), out.getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }catch (Exception e) {
                LOG.error("Failed to write to Splunk destination file", e);
            }
        }
    }

    static {
        Optional<String> splunkFile = Configuration.SPLUNK_FILE;
        splunkDestination = !splunkFile.isPresent() ? Optional.empty() :
                Optional.of(new SplunkDestination(splunkFile.get()));
    }

    private static final Optional<Consumer<String>> splunkDestination;

    private static final ThreadLocal<Long> _timestamps = new ThreadLocal<>();

    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat();

    static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public static void begin() {
        _timestamps.set(System.currentTimeMillis());
    }

    public static void report(Scenario scenario, String app) {
        splunkDestination.ifPresent(dest -> {
            String report = FULL_DATE_FORMAT.format(new Date()) + ' ' + formatReport(scenario, app);

            dest.accept(report);
        });
    }

    private static String formatScenarioImpl(Scenario scenario, Function<Result, String> formatter) {
        if(!scenario.isFailed())
            return "";

        if(scenario instanceof ScenarioImpl) {
            Optional<Result> failure = findFirstFailure((ScenarioImpl) scenario);
            return failure.isPresent() ? formatter.apply(failure.get()) : "No failure found";
        } else {
            LOG.warn("Scenario is not a ScenarioImpl: " + scenario);
            return "Scenario is not a ScenarioImpl";
        }
    }

    static String formatStackTrace(Throwable error) {
        String trace = ExceptionUtils.getStackTrace(error);
        if(trace.endsWith("\r\n"))
            trace = trace.substring(0, trace.length() - 2);
        return trace;
    }

    private static List<Result> accessResults(ScenarioImpl scenario) {
        try {
            Field f = scenario.getClass().getDeclaredField("stepResults"); //NoSuchFieldException
            f.setAccessible(true);
            List<Result> results = (List<Result>) f.get(scenario); //IllegalAccessException]
            return results;
        } catch(Exception e) {
            LOG.error("Failed to access results", e);
            return Collections.emptyList();
        }
    }

    private static Optional<Result> findFirstFailure(ScenarioImpl scenario) {
        for(Result result : accessResults(scenario)) {
            String errorMessage = result.getErrorMessage();
            if(null != errorMessage)
                return Optional.of(result);
        }
        return Optional.empty();
    }

    static String formatReport(Scenario scenario, String app) {
        String env = Configuration.EXECUTION_ENVIRONMENT;
        int executionId = Configuration.EXECUTION_ID.isPresent() ? Configuration.EXECUTION_ID.get() : -1;
        String testName = scenario.getName();
        Date executionDate = new Date();
        long executionTime = calculateElapsedTime();
        boolean passed = !scenario.isFailed();
        boolean grid = Configuration.REMOTE;
        String failureCode = scenario.getStatus();
        String machineName = getComputerName();
        String tagNames = prettyCollection(scenario.getSourceTagNames(), Optional.empty(), " ");
        String exceptionMessage = formatScenarioImpl(scenario, r -> r.getError().getMessage());
        String stackTrace = formatScenarioImpl(scenario, r -> formatStackTrace(r.getError()));

        return new SplunkReportBuilder()
                .append("App", app)
                .append("Env", env, false)
                .append("ExecutionId", executionId)
                .append("TestName", testName)
                .append("ExecutionDate", DAY_FORMAT.format(executionDate))
                .append("ExecutionTime", executionTime)
                .append("Passed", passed)
                .append("FailureCode", failureCode)
                .append("MachineName", machineName)
                .append("Tags", tagNames)
                .append("Exception", exceptionMessage)
                .append("Stacktrace", stackTrace)
                .toString();
    }

    /**
     *
     * @return the elapsed time in millis or -1 if begin() never called.
     */
    private static long calculateElapsedTime() {
        Long beginTime = _timestamps.get();
        _timestamps.remove();

        long elapsedTime = -1;
        if(null != beginTime) {
            elapsedTime = System.currentTimeMillis() - beginTime;
            LOG.info("Elasped time: " + elapsedTime + "ms");

        } else {
            LOG.error("elapsed time not available because begin() never called");
        }
        return elapsedTime;
    }
}
