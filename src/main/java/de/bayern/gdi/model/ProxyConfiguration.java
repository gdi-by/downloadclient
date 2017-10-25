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
package de.bayern.gdi.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Model for proxy configuration. */
@XmlRootElement(name = "ProxyConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProxyConfiguration {

    /** Name of the config file. */
    public static final String PROXY_CONFIG_FILE = "proxy.xml";

    @XmlAttribute(name = "overrideSystemSettings")
    private Boolean overrideSystemSettings;

    @XmlAttribute(name = "enableSNIExtension")
    private Boolean enableSNIExtension;

    @XmlElement(name = "HTTPProxyHost")
    private String httpProxyHost;

    @XmlElement(name = "HTTPProxyPort")
    private Integer httpProxyPort;

    @XmlElement(name = "HTTPProxyUser")
    private String httpProxyUser;

    @XmlElement(name = "HTTPProxyPasswort")
    private String httpProxyPasswort;

    @XmlElement(name = "HTTPNonProxyHosts")
    private String httpNonProxyHosts;

    @XmlElement(name = "HTTPSProxyHost")
    private String httpsProxyHost;

    @XmlElement(name = "HTTPSProxyPort")
    private Integer httpsProxyPort;

    @XmlElement(name = "HTTPSProxyUser")
    private String httpsProxyUser;

    @XmlElement(name = "HTTPSProxyPasswort")
    private String httpsProxyPasswort;

    @XmlElement(name = "HTTPSNonProxyHosts")
    private String httpsNonProxyHosts;

    private static final String NAME = "ProxyConfig";

    public ProxyConfiguration() {
    }

    /**
     * @return the overrideSystemSettings
     */
    public Boolean getOverrideSystemSettings() {
        return overrideSystemSettings;
    }

    /**
     * @param overrideSystemSettings the overrideSystemSettings to set
     */
    public void setOverrideSystemSettings(Boolean overrideSystemSettings) {
        this.overrideSystemSettings = overrideSystemSettings;
    }

    /**
     * @return the httpProxyHost
     */
    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    /**
     * @param httpProxyHost the httpProxyHost to set
     */
    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    /**
     * @return the httpProxyPort
     */
    public Integer getHttpProxyPort() {
        return httpProxyPort;
    }

    /**
     * @param httpProxyPort the httpProxyPort to set
     */
    public void setHttpProxyPort(Integer httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    /**
     * Returns the proxy host and port as string(host:port) if both are
     * defined, else the proxyhost.
     * @return Proxystring
     */
    public String getHttpProxyString() {
        if (!httpProxyPort.equals("") && httpProxyPort != null) {
            return httpProxyHost + ":" + httpProxyPort;
        } else {
            return httpProxyHost;
        }
    }

    /**
     * @return the httpProxyUser
     */
    public String getHttpProxyUser() {
        return httpProxyUser;
    }

    /**
     * @param httpProxyUser the httpProxyUser to set
     */
    public void setHttpProxyUser(String httpProxyUser) {
        this.httpProxyUser = httpProxyUser;
    }

    /**
     * @return the httpProxyPasswort
     */
    public String getHttpProxyPasswort() {
        return httpProxyPasswort;
    }

    /**
     * @param httpProxyPasswort the httpProxyPasswort to set
     */
    public void setHttpProxyPasswort(String httpProxyPasswort) {
        this.httpProxyPasswort = httpProxyPasswort;
    }

    /**
     * @return the httpNonProxyHosts
     */
    public String getHttpNonProxyHosts() {
        return httpNonProxyHosts;
    }

    /**
     * @param httpNonProxyHosts the httpNonProxyHosts to set
     */
    public void setHttpNonProxyHosts(String httpNonProxyHosts) {
        this.httpNonProxyHosts = httpNonProxyHosts;
    }

    /**
     * @return the httpsProxyHost
     */
    public String getHttpsProxyHost() {
        return httpsProxyHost;
    }

    /**
     * @param httpsProxyHost the httpsProxyHost to set
     */
    public void setHttpsProxyHost(String httpsProxyHost) {
        this.httpsProxyHost = httpsProxyHost;
    }

    /**
     * @return the httpsProxyPort
     */
    public Integer getHttpsProxyPort() {
        return httpsProxyPort;
    }

    /**
     * @param httpsProxyPort the httpsProxyPort to set
     */
    public void setHttpsProxyPort(Integer httpsProxyPort) {
        this.httpsProxyPort = httpsProxyPort;
    }
    /**
     * Returns the https proxy host and port as string(host:port) if both are
     * defined, else the proxyhost.
     * @return Proxystring
     */
    public String getHttpsProxyString() {
        if (httpsProxyHost != null
        && !httpsProxyHost.isEmpty()
        && httpsProxyPort != null) {
            return httpsProxyHost + ":" + httpsProxyPort;
        } else {
            return httpsProxyHost;
        }
    }

    /**
     * @return the httpsProxyUser
     */
    public String getHttpsProxyUser() {
        return httpsProxyUser;
    }

    /**
     * @param httpsProxyUser the httpsProxyUser to set
     */
    public void setHttpsProxyUser(String httpsProxyUser) {
        this.httpsProxyUser = httpsProxyUser;
    }

    /**
     * @return the httpsProxyPasswort
     */
    public String getHttpsProxyPasswort() {
        return httpsProxyPasswort;
    }

    /**
     * @param httpsProxyPasswort the httpsProxyPasswort to set
     */
    public void setHttpsProxyPasswort(String httpsProxyPasswort) {
        this.httpsProxyPasswort = httpsProxyPasswort;
    }

    /**
     * @return the httpsNonProxyHosts
     */
    public String getHttpsNonProxyHosts() {
        return httpsNonProxyHosts;
    }

    /**
     * @param httpsNonProxyHosts the httpsNonProxyHosts to set
     */
    public void setHttpsNonProxyHosts(String httpsNonProxyHosts) {
        this.httpsNonProxyHosts = httpsNonProxyHosts;
    }

    /** Apply the settings to the system. */
    public void apply() {

        if (overrideSystemSettings == null || !overrideSystemSettings) {
            return;
        }

        // Configure HTTP:
        if (httpProxyHost != null) {
            System.setProperty("http.proxyHost", httpProxyHost);
        }
        if (httpProxyPort != null) {
            System.setProperty("http.proxyPort", httpProxyPort.toString());
        }
        if (httpProxyUser != null) {
            System.setProperty("http.proxyUser", httpProxyUser);
        }
        if (httpProxyPasswort != null) {
            System.setProperty("http.proxyPasswort", httpProxyPasswort);
        }
        if (httpNonProxyHosts != null) {
            System.setProperty("http.nonProxyHosts", httpNonProxyHosts);
        }

        // Configure HTTPS:
        if (enableSNIExtension != null) {
            System.setProperty(
                "jsse.enableSNIExtension", enableSNIExtension.toString());
        }
        if (httpsProxyHost != null) {
            System.setProperty("https.proxyHost", httpsProxyHost);
        }
        if (httpsProxyPort != null) {
            System.setProperty("https.proxyPort", httpsProxyPort.toString());
        }
        if (httpsProxyUser != null) {
            System.setProperty("https.proxyUser", httpsProxyUser);
        }
        if (httpsProxyPasswort != null) {
            System.setProperty("https.proxyPasswort", httpsProxyPasswort);
        }
        if (httpsNonProxyHosts != null) {
            System.setProperty("https.nonProxyHosts", httpsNonProxyHosts);
        }
    }

    /**
     * Loads ProxyConfiguration from a file.
     * @param file The file to load the ProxyConfiguration from.
     * @return The restored ProxyConfiguration.
     * @throws IOException Something went wrong.
     */
    public static ProxyConfiguration read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            JAXBContext context =
                JAXBContext.newInstance(ProxyConfiguration.class);
            Unmarshaller um = context.createUnmarshaller();
            BufferedInputStream bis = new BufferedInputStream(fis);
            return (ProxyConfiguration)um.unmarshal(bis);
        } catch (JAXBException je) {
            je.printStackTrace();
            throw new IOException(je.getMessage(), je);
        }
    }

    /**
     * gets the name of the configuration.
     * @return name of the config
     */
    public static String getName() {
        return NAME;
    }
}
