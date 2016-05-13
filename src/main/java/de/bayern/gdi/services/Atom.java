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


import java.util.Map;

import java.util.ArrayList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class Atom extends WebService {
    private String serviceURL;

    /**
     * @inheritDoc
     * @return the URL of the Service
     */
    public Atom(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    /**
     * @inheritDoc
     * @return the Types of the service
     */
    public ArrayList<String> getTypes() {
        return null;
    }

    /**
     * @inheritDoc
     * @param type the Type
     * @return The Attributes of the Service
     */
    public Map<String, String> getAttributes(String type) {
        return null;
    }

    /**
     * @inheritDoc
     * @return the URL of the Service
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    /**
     * @inheritDoc
     * @return the Type of the Service
     */
    public WebService.Type getServiceType() {
        return Type.Atom;
    }

}
