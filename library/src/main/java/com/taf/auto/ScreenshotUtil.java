package com.taf.auto;

import com.taf.auto.common.Configuration;
import com.taf.auto.common.ConfigurationKeys;
import cucumber.api.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.taf.auto.IOUtil.formatLegalFilename;
import static com.taf.auto.WebDriverUtil.driver;
import static com.taf.auto.WebDriverUtil.isDriverInitialized;
import static java.lang.String.format;

/**
 * Created by AF04261 on 6/7/2016.
 */
public final class ScreenshotUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotUtil.class);
    public static final int SCREENSHOT_MAX_HEIGHT = 1400;

    private ScreenshotUtil() { /** static only */ }

    public static BufferedImage takeScreenshotScrolling() {

        AShot shot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(100));
        return shot.takeScreenshot(driver()).getImage();
    }

    public static void embedScreenshot(Scenario scenario) {
        WebDriver driver = driver();
        if(driver instanceof TakesScreenshot) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
            LOG.info("Attached screenshot");

        } else {
            LOG.error(format("WebDriver(%s) not instanceof TakesScreenshot", driver.getClass().getName()));
        }
    }

    private static BufferedImage constrainImage(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if(h < SCREENSHOT_MAX_HEIGHT) {
            LOG.debug("Screenshot is below threshold and will be embedded as is");
            return src;
        }
        LOG.info("Screenshot height " + h + " exceeds " + SCREENSHOT_MAX_HEIGHT + " pixels and will be constrained.");
        float ratio = .7f;
        BufferedImage dest = new BufferedImage((int)(w * ratio), (int)(h * ratio), BufferedImage.TYPE_INT_ARGB);
        dest.createGraphics().drawImage(src, 0, 0, dest.getWidth() - 1, dest.getHeight() - 1, 0, 0, w - 1, h - 1, null);

        /* uncomment this code to reduce to 8 bit color
        BufferedImage reducedColor = new BufferedImage(dest.getWidth(), dest.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        reducedColor.createGraphics().drawImage(dest, 0, 0, null);
        dest = reducedColor;
        */

        return dest;
    }

    public static void ifScenarioFailedEmbedScreenshot(Scenario scenario) {
        if(!isDriverInitialized()) {
            LOG.debug("Driver is not initialized, screenshot will noop");
            return;
        }

        if (scenario.isFailed()) {
            if(Configuration.SUPPRESS_SCREENSHOT) {
                LOG.info(format("Screenshot capture suppressed because %s=true.", ConfigurationKeys.SUPPRESS_SCREENSHOT));
                return;
            }

            BufferedImage screenshot;
            try {
                screenshot = takeScreenshotScrolling();
                screenshot = constrainImage(screenshot);
            } catch(Exception e) {
                LOG.error("Unable to capture screenshot", e);
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = null;
            try {
                ImageIO.write(screenshot, "png", baos);
                data = baos.toByteArray();
            } catch (IOException ioe) {
                LOG.warn("Unable to capture screenshot", ioe);
            }
            finally {
                IOUtil.safeClose(baos);
            }
            if(null != data) {
                scenario.embed(data, "image/png");
                LOG.info("Attached screenshot to Scenario");

                captureScreenshotLocally(scenario, data);
            }
        }
    }

    private static void captureScreenshotLocally(Scenario scenario, byte[] data) {
        try {
            Path path = Paths.get("target/screenshots/" + formatLegalFilename(scenario.getId()) + ".png");
            IOUtil.mkdirs(path.getParent());
            LOG.info("Writing screenshot locally to: " + path.toAbsolutePath());
            Files.write(path, data);
        } catch (IOException e) {
            LOG.error("Failed to capture local screenshot for Scenario: " + scenario.getName(), e);
        }
    }
}
