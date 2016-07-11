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
import java.util.logging.Logger;

import org.w3c.dom.Document;

import de.bayern.gdi.model.MIMETypes;
import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.model.ProxyConfiguration;

//import java.util.logging.Level;

/** Load configurations from specified directory. */
public class Config {

    private static final Logger log
        = Logger.getLogger(Config.class.getName());

    /** Inner class to implicit synchronize the instance access. */
    private static final class Holder {
        static final Config INSTANCE = new Config();
    }

    private boolean initialized;

    private ServiceSetting services;

    private MIMETypes mimeTypes;

    private ProcessingConfiguration processingConfig;

    private Config() {
    }

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
            }
            return Holder.INSTANCE;
        }
    }

    /**
     * @return the services
     */
    public ServiceSetting getServices() {
        return services;
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

    /** Mark global config as unused. */
    public static void uninitialized() {
        synchronized (Holder.INSTANCE) {
            common();
            Holder.INSTANCE.services = new ServiceSetting();
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
    public static void load(String dirname) throws IOException {
        synchronized (Holder.INSTANCE) {
            try {
                common();
                loadInternal(dirname);
            } finally {
                Holder.INSTANCE.initialized = true;
                Holder.INSTANCE.notifyAll();
            }
        }
    }

    private static void common() {
        // http://docs.geotools.org/latest/userguide/library/referencing/
        // order.html
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    private static void loadInternal(String dirname) throws IOException {

        log.info("config directory: " + dirname);

        File dir = new File(dirname);

        if (!dir.isDirectory()) {
            throw new IOException("'" + dirname + "' is not a directory.");
        }

        File proxy = new File(dir, ProxyConfiguration.PROXY_CONFIG_FILE);
        if (proxy.isFile() && proxy.canRead()) {
            ProxyConfiguration proxyConfig = ProxyConfiguration.read(proxy);
            proxyConfig.apply();
        }

        File services = new File(dir, ServiceSetting.SERVICE_SETTING_FILE);
        if (services.isFile() && services.canRead()) {
            Document doc = XML.getDocument(services);
            if (doc == null) {
                throw new IOException(
                    "Cannot parse XML file '" + services + "'");
            }
            Holder.INSTANCE.services = new ServiceSetting(doc);
        }

        File procConfig = new File(
            dir, ProcessingConfiguration.PROCESSING_CONFIG_FILE);
        if (procConfig.isFile() && procConfig.canRead()) {
            Holder.INSTANCE.processingConfig =
                ProcessingConfiguration.read(procConfig);
        } else {
            Holder.INSTANCE.processingConfig =
                ProcessingConfiguration.loadDefault();
        }

        File mimeTypes = new File(
            dir, MIMETypes.MIME_TYPES_FILE);
        if (mimeTypes.isFile() && mimeTypes.canRead()) {
            Holder.INSTANCE.mimeTypes =
                MIMETypes.read(mimeTypes);
        } else {
            Holder.INSTANCE.mimeTypes = MIMETypes.loadDefault();
        }

        // TODO: MIME types -> file extensions.
    }
}
