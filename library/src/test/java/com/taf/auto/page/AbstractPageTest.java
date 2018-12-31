package com.taf.auto.page;

import com.taf.auto.common.MockConfiguration;
import com.taf.auto.common.MockConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.taf.auto.page.AbstractPage.install;

/**
 * Unit tests for {@link AbstractPage}.
 *
 * @author AF04261 mmorton
 */
public class AbstractPageTest {

    public static class TestInstallPage extends AbstractPage {
        @Override
        protected By defineUniqueElement() {
            throw new UnsupportedOperationException("This shouldn't get called due to tests being run in MockEnv");
        }
    }

    @BeforeClass
    public static void mockConfig() {
        MockConfiguration.install();
    }

    @Test
    public void installPatternEnforced() {
        install(TestInstallPage.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void lackOfInstallPatternRejected() {
        new TestInstallPage();
    }
}
