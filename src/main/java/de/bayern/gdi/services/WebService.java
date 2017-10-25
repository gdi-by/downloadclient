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

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public abstract class WebService implements ServiceSchema {

    /** The URL of the service. */
    protected String serviceURL;

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
     * gets the service URL.
     * @return the service URL
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    /**
     * @inheritDoc
     * @param typeName the name of the Type
     * @return The description
     */
    public String getDescription(String typeName) {
        return null;
    }
}
