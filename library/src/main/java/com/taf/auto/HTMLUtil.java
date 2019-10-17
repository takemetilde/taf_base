package com.taf.auto;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import static com.taf.auto.WebDriverUtil.driver;

/**
 * Utility methods related to HTML.
 *
 */
public final class HTMLUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HTMLUtil.class);

    private HTMLUtil() {
        /** static only */
    }

    /**
     * Uses the {@link WebDriver} to scan the current page and collects all the image and anchor tags.
     *
     * @return a collection of all links (both <tt>a</tt> and <tt>img</tt>) on the page
     */
    public static Collection<WebElement> findAllLinks() {
        WebDriver driver = driver();
        List<WebElement> as = driver.findElements(By.tagName("a"));
        List<WebElement> imgs = driver.findElements(By.tagName("img"));
        int numLinks = as.size() + imgs.size();
        Collection<WebElement> links = new ArrayList<>(numLinks);
        links.addAll(as);
        links.addAll(imgs);
        LOG.info("Found " + numLinks + " links.");
        return links;
    }

    public static boolean isLinkBroken(String urlTxt) {
        LOG.debug("Testing URL: " + urlTxt);
        try {
            URL url = new URL(urlTxt);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            LOG.trace("Connecting...");
//            connection.setConnectTimeout(4000);
//            connection.setReadTimeout(4000);
            connection.connect();
            LOG.trace("Getting response code...");
            int responseCode = connection.getResponseCode();
            LOG.debug("Response code: " + responseCode);
            connection.disconnect();
            switch (responseCode) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return true;
            }
            return false;
        } catch (MalformedURLException murle) {
            LOG.warn("URL malformed: " + urlTxt);
            return true;
        } catch (IOException ioe) {
            LOG.warn("URL unavailable: " + urlTxt, ioe);
            return true;
        }
    }

    public static Collection<WebElement> findBrokenLinks(Collection<WebElement> elements) {
        Collection<WebElement> broken = new ArrayList<>();
        for (WebElement link : elements) {
            String href = link.getAttribute("href");
            if (null != href) {
                if (isLinkBroken(href)) {
                    LOG.debug("Found broken link: " + href);
                    broken.add(link);
                }
            }
        }
        return broken;
    }

    public static Collection<WebElement> findAllBrokenLinks() {
        return findBrokenLinks(findAllLinks());
    }

    public static String loadURL(String rawURL) throws IOException {
        LOG.trace("Loading URL: " + rawURL);
        URL url;
        try {
            url = new URL(rawURL);
        } catch (IOException ioe) {
            throw new IOException("Invalid URL: " + rawURL);
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(url.openStream(), "UTF-8");
            scanner.useDelimiter("\\A");
            String content = scanner.next();
            LOG.trace("URL: " + rawURL + " returns:\n" + content);
            return content;
        } catch (IOException ioe) {
            throw new IOException("Unable to scan URL: " + rawURL, ioe);
        }
        finally {
            IOUtil.safeClose(scanner);
        }
    }
}
