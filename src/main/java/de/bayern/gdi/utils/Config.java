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

import org.w3c.dom.Document;

import de.bayern.gdi.model.ProcessingConfiguration;
import de.bayern.gdi.model.ProxyConfiguration;

/** Load configurations from specified directory. */
public class Config {

    private static Config instance;

    private ServiceSetting services;

    private ProcessingConfiguration processingConfig;

    private Config() {
    }

    /**
     * Access the instance of the configuration.
     * Use #load before to initialize it.
     * @return The configuration instance.
     */
    public static synchronized Config getInstance() {
        return instance;
    }

    /**
     * @return the services
     */
    public ServiceSetting getServices() {
        return services;
    }

    /**
     * @return the processingConfig
     */
    public ProcessingConfiguration getProcessingConfig() {
        return processingConfig;
    }

    /**
     * Load configurations.
     * @param dirname The directory with the configuration files.
     * @throws IOException If something went wrong.
     */
    public static synchronized void load(String dirname) throws IOException {

        File dir = new File(dirname);

        if (!dir.isDirectory()) {
            throw new IOException("'" + dirname + "' is not a directory.");
        }

        File proxy = new File(dir, ProxyConfiguration.PROXY_CONFIG_FILE);
        if (proxy.isFile() && proxy.canRead()) {
            ProxyConfiguration proxyConfig = ProxyConfiguration.read(proxy);
            proxyConfig.apply();
        }

        instance = new Config();

        File services = new File(dir, ServiceSetting.SERVICE_SETTING_FILE);
        if (services.isFile() && services.canRead()) {
            Document doc = XML.getDocument(services);
            if (doc == null) {
                throw new IOException(
                    "Cannot parse XML file '" + services + "'");
            }
            instance.services = new ServiceSetting(doc);
        }

        File procConfig = new File(
            dir, ProcessingConfiguration.PROCESSING_CONFIG_FILE);
        if (procConfig.isFile() && procConfig.canRead()) {
            instance.processingConfig =
                ProcessingConfiguration.read(procConfig);
        }

        // TODO: MIME types -> file extensions.
    }
}
