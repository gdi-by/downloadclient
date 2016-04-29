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

import org.opengis.feature.type.AttributeType;

import java.util.ArrayList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WFSOne extends WebService {
    private String serviceURL;

    /**
     * Constructor.
     * @param serviceURL the service URL
     */
    public WFSOne(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    /**
     * gets the Types of this service.
     * @return types of the service
     */
    public ArrayList<String> getTypes() {
        return null;
    }

    /**
     * gets the attributes of a type.
     * @param type the type
     * @return the attributes
     */
    public ArrayList<AttributeType> getAttributes(String type) {
        return null;
    }

    /**
     * returns the services URL.
     * @return service URL
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

}
