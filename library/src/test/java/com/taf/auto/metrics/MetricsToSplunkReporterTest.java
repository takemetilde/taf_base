package com.taf.auto.metrics;

import com.taf.auto.common.ConfigurationPropertyAdapter;
import com.taf.auto.common.MockConfiguration;
import com.taf.auto.common.MockConfiguration;
import cucumber.api.Scenario;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static com.taf.auto.common.Configuration.getComputerName;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link MetricsToSplunkReporter}.
 *
 * @author AF04261 mmorton
 */
public class MetricsToSplunkReporterTest {
    @Test
    public void formatting() {
        Scenario scenario = new Scenario() {
            @Override
            public Collection<String> getSourceTagNames() {
                return Collections.singletonList("@mockTag");
            }

            @Override
            public String getStatus() {
                return "So Failed";
            }

            @Override
            public boolean isFailed() {
                return true;
            }

            @Override
            public void embed(byte[] data, String mimeType) {

            }

            @Override
            public void write(String text) {

            }

            @Override
            public String getName() {
                return "MockScenario";
            }

            @Override
            public String getId() {
                return null;
            }
        };

        MockConfiguration.install();
        String report = MetricsToSplunkReporter.formatReport(scenario, "TestApp");
        String today = MetricsToSplunkReporter.DAY_FORMAT.format(new Date());
        String machineName = getComputerName();
        String expected = String.format("App=\"TestApp\", Env=MOCKENV, ExecutionId=-1, TestName=\"MockScenario\", ExecutionDate=\"%s\", ExecutionTime=-1, Passed=false, FailureCode=\"So Failed\", MachineName=\"%s\", Tags=\"@mockTag\", Exception=\"Scenario is not a ScenarioImpl\", Stacktrace=\"Scenario is not a ScenarioImpl\"", today, machineName);
        assertEquals(expected, report);
    }
}
