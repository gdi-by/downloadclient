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

    @XmlAttribute(name = "overrideSystemSettings")
    private boolean overrideSystemSettings;

    @XmlElement(name = "ProxyHost")
    private String proxyHost;

    @XmlElement(name = "ProxyPort")
    private int proxyPort;

    @XmlElement(name = "ProxyUser")
    private String proxyUser;

    @XmlElement(name = "ProxyPasswort")
    private String proxyPasswort;

    @XmlElement(name = "NonProxyHosts")
    private String nonProxyHosts;

    public ProxyConfiguration() {
    }

    /**
     * @return the overrideSystemSettings
     */
    public boolean isOverrideSystemSettings() {
        return overrideSystemSettings;
    }

    /**
     * @param overrideSystemSettings the overrideSystemSettings to set
     */
    public void setOverrideSystemSettings(boolean overrideSystemSettings) {
        this.overrideSystemSettings = overrideSystemSettings;
    }

    /**
     * @return the proxyHost
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * @param proxyHost the proxyHost to set
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * @return the proxyPort
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * @param proxyPort the proxyPort to set
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @return the proxyPasswort
     */
    public String getProxyPasswort() {
        return proxyPasswort;
    }

    /**
     * @param proxyPasswort the proxyPasswort to set
     */
    public void setProxyPasswort(String proxyPasswort) {
        this.proxyPasswort = proxyPasswort;
    }

    /**
     * @return the nonProxyHosts
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * @param nonProxyHosts the nonProxyHosts to set
     */
    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
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
            throw new IOException("", je);
        }
    }
}
