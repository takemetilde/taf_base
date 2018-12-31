package com.taf.auto.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.function.Consumer;

import static com.taf.auto.IOUtil.safeClose;

/**
 * For unit tests, this harness allows circumvention of the typical loading of
 * the properties from "system.properties" by supplying a alternate logic via
 * {@link #overridePropertyAdapter}.
 *
 */
public final class ConfigurationPropertyAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationPropertyAdapter.class);

    private ConfigurationPropertyAdapter() { /** static only */ }

    static void populateProperties(Properties prop) {
        try {
            adapter.accept(prop);
        } catch(Exception e) {
            LOG.error("Failed to adapt properties with adapter: " + adapter, e);
        }
    }

    private static class DefaultPropertyAdapter implements Consumer<Properties> {

        @Override
        public void accept(Properties prop) {
            URL resource = Res.getResource("system.properties");
            InputStream input = null;
            try {
                File file = new File(resource.toURI());
                if(!file.isFile())
                    throw new IOException("Property file missing: " + file);
                input = new FileInputStream(file);
                prop.load(input);
            } catch (Exception e) {
                LOG.error("Failed to load " + resource, e);
            } finally {
                safeClose(input);
            }
        }
    }

    private static Consumer<Properties> adapter = new DefaultPropertyAdapter();

    public static void overridePropertyAdapter(Consumer<Properties> adapterOverride) {
        LOG.info("Overriding property adapter: " + adapterOverride);
        adapter = adapterOverride;
    }
}
