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
    private final static String ATOM = "atom";
    private final static String WFSONE = "wfs 1";
    private final static String WFSTWO = "wfs 2";

    private URL serviceURL;
    private ServiceType serviceType;
    private String additionalMessage;
    private String username;
    private String password;
    private String name;
    private boolean restricted;

    private Service() {}

    public Service (URL url) {
        this(url, url.toString());
    }

    public Service (URL url,
                    String name) {
        this(url, name, ServiceChecker.isRestricted(url));
    }

    public Service (URL url,
                    String name,
                    boolean restricted) {
        this(url, name, restricted, "", "");
    }

    public Service (URL url,
                    String name,
                    String username,
                    String password) {
        this (url, name, ServiceChecker.isRestricted(url), username, password);
    }

    public Service (URL url,
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

    public Service (URL url,
                    String name,
                    boolean restricted,
                    String serviceType) {
        this (url, name, restricted, guessServiceType(serviceType));
    }

    public Service (URL url,
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

    public Service (URL url,
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

    public void setUsername(String username) {
        this.username = username;
        this.loaded = false;
    }

    public void setPassword(String password) {
        this.password = password;
        this.loaded = false;
    }

    public URL getServiceURL() {
        return serviceURL;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getAdditionalMessage() {
        return additionalMessage;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void load() throws IOException{
        if (!this.loaded) {
            this.additionalMessage = new String();
            int headStatus = ServiceChecker.tryHead(serviceURL);
            if (headStatus == HttpStatus.SC_OK) {
                this.restricted = ServiceChecker.isRestricted(this.serviceURL);
                this.serviceType = null;
                checkServiceType();
                if (this.serviceType != null) {
                    return;
                }
            } else if (headStatus == HttpStatus.SC_UNAUTHORIZED) {
                this.restricted = true;
                checkServiceType();
                if (serviceType != null) {
                    return;
                }
            }
            // Only append to the URL if the Service is not restricted.
            if (!this.restricted) {
                URL newURL = guessURL(this.serviceURL);
                if (newURL.equals(this.serviceURL)) {
                    additionalMessage = "The URL is not reachable";
                } else {
                    this.serviceURL = newURL;
                    checkServiceType();
                    if (serviceType != null) {
                        return;
                    }
                }
            }
            additionalMessage = "The service could not be determined";
            loaded = true;
        }
    }

    private static URL guessURL(URL url) throws MalformedURLException {
        String urlStr = url.toString();
        if (urlStr.toLowerCase().contains(WFS_URL_EXPR)
                && urlStr.toLowerCase().contains(GET_CAP_EXPR)) {
            return url;
        } else {
            return new URL(urlStr + URL_TRY_APPENDIX);
        }
    }

    private void checkServiceType() {
        if (this.isRestricted()) {
            if (this.username!= null && this.password != null) {
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

    public int hashCode() {
        int code = 0;
        code += this.name.hashCode();
        code += this.serviceURL.hashCode();
        code += this.username.hashCode();
        code += this.password.hashCode();
        code += this.serviceType.hashCode();
        if (this.loaded)
            code++;
        return code;
    }

    public boolean equals(Service s) {
        if (s.hashCode() == this.hashCode()) {
            return true;
        }
        return false;
    }
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
