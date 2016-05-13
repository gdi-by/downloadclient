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

import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public abstract class WebService implements ServiceSchema {
    /**
     * Types of Webservices.
     */
    public enum Type {
        /**
         * WFSOne Webservice.
         */
        WFSOne,
        /**
         * WFSTwo Webservice.
         */
        WFSTwo,
        /**
         * Atom Webservice.
         */
        Atom
    }
    private String serviceURL;

    private String serviceType;

    /**
     * Constructor.
     */
    public WebService() {
    }

    /**
     * Constructor.
     * @param serviceURL URL to the Service
     */
    public WebService(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    /**
     * @inheritDoc
     * @return NULL
     */
    public ArrayList<String> getStoredQueries() {
        return null;
    }

    /**
     * @inheritDoc
     * @return NULL
     */
    public ArrayList<String> getRequestMethods() {
        return null;
    }

    /**
     * @inheritDoc
     * @return NULL
     * @param queryName The Name of the query
     */
    public Map<String, String> getParameters(String queryName) {
        return null;
    }

    /**
     * gets the username and password as base64 encoded string.
     * @param userName the username
     * @param password the password
     * @return the base64 encoded string
     */
    protected String getBase64EncAuth(String userName, String password) {
        if (userName == null || password == null) {
            return null;
        } else {
            String authString = userName + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            return authStringEnc;
        }
    }
}
