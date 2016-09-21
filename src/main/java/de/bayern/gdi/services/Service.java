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

package de.bayern.gdi.services;

import de.bayern.gdi.utils.ServiceChecker;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpStatus;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Service extends Object {

    private boolean loaded;
    private static final String WFS_URL_EXPR = "wfs";
    private static final String GET_CAP_EXPR = "getcapabilities";
    private static final String URL_TRY_APPENDIX =
            "?service=wfs&acceptversions=2.0.0&request=GetCapabilities";
    private static final String ATOM = "atom";
    private static final String WFSONE = "wfs 1";
    private static final String WFSTWO = "wfs 2";

    private URL serviceURL;
    private ServiceType serviceType;
    private String additionalMessage;
    private String username;
    private String password;
    private String name;
    private boolean restricted;

    private Service() {

    }

    public Service(URL url) {
        this(url, url.toString());
    }

    public Service(URL url,
                   String name) {
        this(url, name, ServiceChecker.isRestricted(url));
    }

    public Service(URL url,
                   String name,
                   boolean restricted) {
        this(url, name, restricted, "", "");
    }

    public Service(URL url,
                   String name,
                   String username,
                   String password) {
        this (url, name, ServiceChecker.isRestricted(url), username, password);
    }

    public Service(URL url,
                   String name,
                   boolean restricted,
                   String username,
                   String password) {
        this.serviceURL = url;
        this.name = name;
        this.restricted = restricted;
        this.loaded = false;
        this.username = username;
        this.password = password;
    }

    public Service(URL url,
                   String name,
                   boolean restricted,
                   String serviceType) {
        this (url, name, restricted, guessServiceType(serviceType), "", "");
    }

    public Service(URL url,
                   String name,
                   boolean restricted,
                   ServiceType serviceType) {
        this(url,
                name,
                restricted,
                serviceType,
                "",
                "");
    }

    public Service(URL url,
                   String name,
                   boolean restricted,
                   ServiceType serviceType,
                   String username,
                   String password) {
        this.serviceURL = url;
        this.name = name;
        this.restricted = restricted;
        this.username = username;
        this.password = password;
        if (serviceType != null) {
            this.serviceType = serviceType;
            this.loaded = true;
        }  else {
            this.loaded = false;
        }
    }

    /**
     * Sets the Username.
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
        this.loaded = false;
    }

    /**
     * Sets the Password.
     * @param password the Pasword
     */
    public void setPassword(String password) {
        this.password = password;
        this.loaded = false;
    }

    /**
     * gets the URL.
     * @return the URL.
     */
    public URL getServiceURL() {
        return serviceURL;
    }

    /**
     * gehts the service Type.
     * @return the service Type.
     */
    public ServiceType getServiceType() {
        return serviceType;
    }

    /**
     * returns an additional message.
     * @return additional message
     */
    public String getAdditionalMessage() {
        return additionalMessage;
    }

    /**
     * gets the username.
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * gets the password.
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * gets the name.
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * returns restricition.
     * @return true if restricted; false if not
     */
    public boolean isRestricted() {
        return restricted;
    }

    /**
     * loads all additional infos.
     * @throws IOException when something goes worng
     */
    public void load() throws IOException {
        if (!this.loaded) {
            try {
                this.additionalMessage = new String();
                int headStatus = ServiceChecker.tryHead(serviceURL);
                if (headStatus == HttpStatus.SC_OK) {
                    this.restricted = ServiceChecker.isRestricted(
                            this.serviceURL
                    );
                    if (this.serviceType == null) {
                        checkServiceType();
                        if (this.serviceType == null) {
                            if (!checkURLOptionsAndSetType()) {
                                return;
                            }
                        }
                    }
                } else if (headStatus == HttpStatus.SC_UNAUTHORIZED) {
                    this.restricted = true;
                    checkServiceType();
                    if (serviceType == null) {
                        if (!checkURLOptionsAndSetType()) {
                            return;
                        }
                    }
                } else {
                    if (!checkURLOptionsAndSetType()) {
                        return;
                    }
                }
                additionalMessage = "The service could not be determined";
            } finally {
                loaded = true;
            }
        }
    }

    private boolean checkURLOptionsAndSetType() {
        try {
            URL newURL = guessURL(this.serviceURL);
            if (!newURL.equals(this.serviceURL)) {
                if (ServiceChecker.isReachable(newURL)) {
                    ServiceType st;
                    if (ServiceChecker.isRestricted(newURL)) {
                        st = ServiceChecker.checkService(newURL, this
                                .username, this.password);
                    } else {
                        st = ServiceChecker.checkService(newURL);
                    }
                    if (st != null) {
                        this.serviceURL = newURL;
                        this.serviceType = st;
                        return true;
                    }
                }
            }
        } catch (Exception e ) {
                return false;
        }
        return false;
    }

    private static URL guessURL(URL url) throws MalformedURLException {
        String urlStr = url.toString();
        if (urlStr.toLowerCase().contains(WFS_URL_EXPR)
                && urlStr.toLowerCase().contains(GET_CAP_EXPR)) {
            return url;
        } else {
            if (urlStr.endsWith("?")) {
                urlStr = urlStr.substring(0, urlStr.lastIndexOf("?"));
            }
            return new URL(urlStr + URL_TRY_APPENDIX);
        }
    }

    private void checkServiceType() {
        if (this.isRestricted()) {
            if (this.username != null && this.password != null) {
                if (!this.username.isEmpty()
                        && !this.password.isEmpty()) {
                    this.serviceType = ServiceChecker.checkService(
                            this.serviceURL,
                            this.username,
                            this.password);
                }
            }
        } else {
            this.serviceType = ServiceChecker.checkService(this.serviceURL,
                    null,
                    null);
        }
    }

    /**
     * returns the hashcode.
     * @return the hashcode
     */
    public int hashCode() {
        int code = 0;
        if (this.name != null) {
            code += this.name.hashCode();
        }
        if (this.serviceURL != null) {
            code += this.serviceURL.hashCode();
        }
        if (this.username != null) {
            code += this.username.hashCode();
        }
        if (this.password != null) {
            code += this.password.hashCode();
        }
        if (this.serviceType != null) {
            code += this.serviceType.hashCode();
        }
        return code;
    }

    /**
     * checks if given object is equal.
     * @param s given object
     * @return true if equal; false if not
     */
    public boolean equals(Service s) {
        if (s != null) {
            if (s.hashCode() == this.hashCode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if the object is loded.
     * @return true if loaded; false if not
     */
    public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * guesses the service Type based on String.
     * @param typeString the string
     * @return service Type
     */
    public static ServiceType guessServiceType(String typeString) {
        typeString = typeString.toLowerCase();
        if (typeString.contains(ATOM)) {
            return ServiceType.Atom;
        } else if (typeString.contains(WFSONE)) {
            return ServiceType.WFSOne;
        } else if (typeString.contains(WFSTWO)) {
            return ServiceType.WFSTwo;
        } else {
            return null;
        }
    }
}
