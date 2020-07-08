/*
 * DownloadClient Geodateninfrastruktur Bayern
 *
 * (c) 2016 GSt. GDI-BY (gdi.bayern.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bayern.gdi.utils;

import java.io.File;
import java.io.IOException;

import de.bayern.gdi.gui.controller.Controller;
import de.bayern.gdi.model.MIMETypes;
import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.model.ProxyConfiguration;

import org.geotools.util.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/** Load configurations from specified directory. */
public class Config {

    private Config() {
    }

    private static final Logger log
        = LoggerFactory.getLogger(Config.class.getName());

    /** Inner class to implicit synchronize the instance access. */
    private static final class Holder {
        private Holder() {
        }
        static final Config INSTANCE = new Config();
    }

    private boolean initialized;

    private Settings settings;

    private MIMETypes mimeTypes;

    private ProcessingConfiguration processingConfig;

    private ProxyConfiguration proxyConfig;

    private static final long FIVEHUNDRED = 500;

    /**
     * Access the instance of the configuration.
     * Use #load before to initialize it.
     * @return The configuration instance.
     */
    public static Config getInstance() {
        synchronized (Holder.INSTANCE) {
            try {
                while (!Holder.INSTANCE.initialized) {
                    Holder.INSTANCE.wait(FIVEHUNDRED);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return Holder.INSTANCE;
        }
    }

    /**
     * @return The application Settings
     */
    public ApplicationSettings getApplicationSettings() {
        return settings.getApplicationSettings();
    }

    /**
     * @return the services
     */
    public ServiceSettings getServices() {
        return settings.getServiceSettings();
    }

    /**
     * @return the mimeTypes
     */
    public MIMETypes getMimeTypes() {
        return mimeTypes;
    }

    /**
     * @return the processingConfig
     */
    public ProcessingConfiguration getProcessingConfig() {
        return processingConfig;
    }

    /**
     * @return the proxy config
     */
    public ProxyConfiguration getProxyConfig() {
        return proxyConfig;
    }

    @Override
    public String toString() {
        return (proxyConfig == null ? "Proxy Settings: {}" : getProxyConfig())
            + ",\n " + getApplicationSettings()
            + ",\n " + getProcessingConfig()
            + ",\n " + getServices()
            + ",\n MIME-Types: " + getMimeTypes();
    }

    private static Settings loadSettings(File file)
        throws IOException {
        try {
            if (file != null && file.isFile() && file.canRead()) {
                return new Settings(file);
            }
            return new Settings();
        } catch (SAXException
                | ParserConfigurationException
                | IOException e) {
            log.error(e.getMessage(), Holder.INSTANCE);
            throwConfigFailureException(Settings.getName());
        }
        // Not reached.
        return null;
    }

    /**
     * Mark global config as unused.
     * @throws IOException when anythong goes wrong
     */
    private static void uninitialized()
        throws IOException {
        synchronized (Holder.INSTANCE) {
            log.info("No config directory given, starting with standard "
                    + "values...");
            Holder.INSTANCE.settings = loadSettings(null);
            Holder.INSTANCE.processingConfig =
                ProcessingConfiguration.loadDefault();
            Holder.INSTANCE.mimeTypes = MIMETypes.loadDefault();
            Holder.INSTANCE.initialized = true;
            Holder.INSTANCE.notifyAll();
        }
    }

    /**
     * Load configurations.
     * @param dirname The directory with the configuration files.
     * @throws IOException If something went wrong.
     */
    public static void initialize(String dirname) throws IOException {
        Controller.logToAppLog(I18n.format("dlc.start", Info.getVersion()));
        System.setProperty("java.util.logging.manager",
            "org.apache.logging.log4j.jul.LogManager");
        try {
            Logging.GEOTOOLS.setLoggerFactory(
                "org.geotools.util.logging.Log4JLoggerFactory");
        } catch (ClassNotFoundException e) {
            log.warn("Failed to initialize GeoTools logging subsystem: "
                + e.getLocalizedMessage());
        }
        if (dirname == null) {
            uninitialized();
        } else {
            synchronized (Holder.INSTANCE) {
                try {
                    load(dirname);
                } finally {
                    Holder.INSTANCE.initialized = true;
                    Holder.INSTANCE.notifyAll();
                }
            }
        }
        log.trace("System Properties:");
        System.getProperties().keySet().stream().map(
            k -> (String)k).sorted().forEach(k -> {
            log.trace(String.format("\t%s=%s", k,
                System.getProperty(k)));
        });
        Controller.logToAppLog(I18n.format("dlc.config", Config.getInstance()));
    }

    private static void load(String dirname) throws IOException {
        log.info("config directory: " + dirname);

        File dir = new File(dirname);

        if (!dir.isDirectory()) {
            throw new IOException("'" + dirname + "' is not a directory.");
        }

        File proxy = new File(dir, ProxyConfiguration.PROXY_CONFIG_FILE);
        if (proxy.isFile() && proxy.canRead()) {
            Holder.INSTANCE.proxyConfig = ProxyConfiguration.read(proxy);
            Holder.INSTANCE.proxyConfig.apply();
        } else {
            log.info("No Proxy config found, starting without proxy.");
        }

        File services = new File(dir, Settings.SETTINGS_FILE);
        if (services.isFile() && services.canRead()) {
            Holder.INSTANCE.settings = loadSettings(services);
        } else {
            Holder.INSTANCE.settings = loadSettings(null);
            log.info("Settings config not found, using fallback...");
        }

        File procConfig = new File(
            dir, ProcessingConfiguration.PROCESSING_CONFIG_FILE);
        if (procConfig.isFile() && procConfig.canRead()) {
            Holder.INSTANCE.processingConfig =
                ProcessingConfiguration.read(procConfig);
        } else {
            Holder.INSTANCE.processingConfig =
                ProcessingConfiguration.loadDefault();
            log.info("Processing config not found, using fallback...");
        }

        if (Holder.INSTANCE.getProcessingConfig() == null) {
            throwConfigFailureException(ProcessingConfiguration.getName());
        }

        File mimeTypes = new File(dir, MIMETypes.MIME_TYPES_FILE);
        if (mimeTypes.isFile() && mimeTypes.canRead()) {
            Holder.INSTANCE.mimeTypes =
                MIMETypes.read(mimeTypes);
        } else {
            Holder.INSTANCE.mimeTypes = MIMETypes.loadDefault();
            log.info("MimeTypes config not found, using fallback...");
        }

        if (Holder.INSTANCE.getMimeTypes() == null) {
            throwConfigFailureException(MIMETypes.getName());
        }
    }

    private static void throwConfigFailureException(String configName)
        throws IOException {
        throw new IOException(
                "Failed on XML file for '"
                        + configName
                        + "'");
    }
}
