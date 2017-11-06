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
import de.bayern.gdi.utils.StringUtils;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Service extends Object {

    private boolean loaded;
    private static final String WFS_URL_EXPR = "wfs";
    private static final String GET_CAP_EXPR = "getcapabilities";
    private static final String URL_TRY_APPENDIX =
            "?service=wfs&acceptversions=2.0.0&request=GetCapabilities";

    private URL serviceURL;
    private ServiceType serviceType;
    private String additionalMessage;
    private String username;
    private String password;
    private String name;
    private boolean restricted;

    private Service() {

    }

    public Service(URL url,
                   String name,
                   boolean restricted) {
        this(url, name, restricted, "", "");
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
            this.serviceType = null;
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
        if (this.loaded) {
            return;
        }
        try {
            this.additionalMessage = "";
            int headStatus = ServiceChecker.tryHead(serviceURL);
            if (headStatus == HttpStatus.SC_OK) {
                this.restricted = ServiceChecker.isRestricted(
                        this.serviceURL
                );
                //Checking for validity before?!
                if (this.serviceType == null) {
                    checkServiceType();
                    if (this.serviceType == null
                    && !checkURLOptionsAndSetType()) {
                        return;
                    }
                }
            } else if (headStatus == HttpStatus.SC_UNAUTHORIZED) {
                this.restricted = true;
                checkServiceType();
                if (serviceType == null
                && !checkURLOptionsAndSetType()) {
                    return;
                }
            } else {
                return;
            }
            additionalMessage = "The service could not be determined";
        } finally {
            loaded = true;
        }
    }

    private boolean checkURLOptionsAndSetType() {
        try {
            URL newURL = guessURL(this.serviceURL);
            if (!newURL.equals(this.serviceURL)
            && ServiceChecker.isReachable(newURL)) {

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
        } catch (Exception e) {
                return false;
        }
        return false;
    }

    private static URL guessURL(URL url) throws MalformedURLException {
        String urlStr = url.toString();
        if (urlStr.toLowerCase().contains(WFS_URL_EXPR)
        && urlStr.toLowerCase().contains(GET_CAP_EXPR)) {
            return url;
        }
        if (urlStr.endsWith("?")) {
            urlStr = urlStr.substring(0, urlStr.lastIndexOf('?'));
        }
        return new URL(urlStr + URL_TRY_APPENDIX);
    }

    private void checkServiceType() {
        if (this.isRestricted()) {
            if (this.username != null
            && this.password != null
            && !this.username.isEmpty()
            && !this.password.isEmpty()) {
                this.serviceType = ServiceChecker.checkService(
                    this.serviceURL,
                    this.username,
                    this.password);
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
    @Override
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
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Service)) {
            return false;
        }
        Service s = (Service)o;
        return StringUtils.nullOrEquals(this.name, s.name)
            && nullOrEquals(this.serviceURL, s.serviceURL)
            && StringUtils.nullOrEquals(this.username, s.username)
            && ServiceType.nullOrEquals(this.serviceType, s.serviceType);
    }

    private static boolean nullOrEquals(URL a, URL b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    /**
     * checks if the object is loded.
     * @return true if loaded; false if not
     */
    public boolean isLoaded() {
        return this.loaded;
    }
}
