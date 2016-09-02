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

import de.bayern.gdi.services.ServiceType;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class ServiceMetaInformation {
    private URL serviceURL;
    private ServiceType serviceType;
    private String additionalMessage;
    private String username;
    private String password;
    private Boolean restricted;


    /**
     * Constructor.
     * @param url the url
     * @throws MalformedURLException if url is wrong
     */
    public ServiceMetaInformation(String url)
        throws MalformedURLException {
        this(new URL(url));
    }

    /**
     * Constructor.
     * @param url the url
     */
    public ServiceMetaInformation(URL url) {
        this(url, new String(), new String());
    }

    /**
     * Constructor.
     * @param url the url
     * @param username username
     * @param password password
     * @throws MalformedURLException if url is wrong
     */
    public ServiceMetaInformation(String url,
                                  String username,
                                  String password)
            throws MalformedURLException {
        this(new URL(url), username, password);
    }

    /**
     * Constructor.
     * @param url the url
     * @param username username
     * @param password password
     */
    public ServiceMetaInformation(URL url,
                                  String username,
                                  String password) {
        this.username = username;
        this.password = password;
        this.serviceURL = url;
    }

    /**
     * Constructor.
     * @param url the url
     * @param serviceType service Type
     * @throws MalformedURLException if url is wrong
     */
    public ServiceMetaInformation(String url,
                                  ServiceType serviceType)
        throws MalformedURLException {
        this(url, serviceType, new String());
    }

    /**
     * Constructor.
     * @param url the url
     * @param serviceType service Type
     * @param additionalMessage any additional message
     * @throws MalformedURLException if url is wrong
     */
    public ServiceMetaInformation(String url,
                                  ServiceType serviceType,
                                  String additionalMessage)
        throws MalformedURLException {
        this(new URL(url), serviceType, additionalMessage);
    }

    /**
     * Constructor.
     * @param url the url
     * @param serviceType service Type
     * @throws MalformedURLException if url is wrong
     */
    public ServiceMetaInformation(URL url,
                                  ServiceType serviceType) {
        this(url, serviceType, null);
    }

    /**
     * Constructor.
     * @param url the url
     * @param serviceType service Type
     * @param additionalMessage any additional message
     * @throws MalformedURLException if url is wrong
     */
    public ServiceMetaInformation(URL url,
                                  ServiceType serviceType,
                                  String additionalMessage) {
        this(url,
                serviceType,
                additionalMessage,
                new String(),
                new String(),
                false);
    }

    /**
     * Constructor.
     * @param url the url
     * @param serviceType service Type
     * @param additionalMessage any additional message
     * @throws MalformedURLException if url is wrong
     */
    public ServiceMetaInformation(URL url,
                                  ServiceType serviceType,
                                  String additionalMessage,
                                  String username,
                                  String password,
                                  Boolean restricted) {
        this.username = username;
        this.password = password;
        this.serviceURL = url;
        this.restricted = restricted;
        this.serviceType = serviceType;
        this.additionalMessage = additionalMessage;
    }

    /**
     * gets the services URL.
     * @return the url of the service
     */
    public URL getServiceURL() {
        return this.serviceURL;
    }

    /**
     * gets the type of the service.
     * @return type of service
     */
    public ServiceType getServiceType() {
        return this.serviceType;
    }

    /**
     * if service is restricted.
     * @return if service is restriced.
     */
    public Boolean isRestricted() {
        return this.restricted;
    }

    /**
     * gets any additional message.
     * @return additional message.
     */
    public String getAdditionalMessage() {
        return this.additionalMessage;
    }

    /**
     * gets the Username.
     * @return username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * gets the Password.
     * @return password
     */
    public String getPassword() {
        return this.password;
    }
}
