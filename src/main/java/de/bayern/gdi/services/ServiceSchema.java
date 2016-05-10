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
interface ServiceSchema {
    /**
     * gets the Types of a Service.
     * @return ArrayList of Types
     */
    ArrayList<String> getTypes();

    /**
     * gets the Attributes of a Type.
     * @param type the Type
     */
    Map<String, String> getAttributes(String type);

    /**
     * gets the URL of a service.
     */
    String getServiceURL();

    /**
     * gets stored Queries of a Service.
     */
    ArrayList<String> getStoredQueries();

    /**
     * gets the Request Methods of a Service.
     */
    ArrayList<String> getRequestMethods();

    /**
     * gets the Parameters for a Stored Query.
     */
    Map<String, String> getParameters(String queryName);

    /**
     * gets the ServiceType.
     */
    WebService.Type getServiceType();
}
