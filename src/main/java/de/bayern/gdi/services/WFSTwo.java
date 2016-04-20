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

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WFSTwo extends WebService {

    private static final Logger log
        = Logger.getLogger(WFSTwo.class.getName());

    private String serviceURL;

    private ArrayList<String> types;
    private DataStore data;

    /**
     * Constructor.
     * @param serviceURL URL to Service
     */
    public WFSTwo(String serviceURL) {
        this.serviceURL = serviceURL;

        Map connectionParameters = new HashMap();
        connectionParameters.put(
                "WFSDataStoreFactory:GET_CAPABILITIES_URL", this.serviceURL);

        try {
            this.data = DataStoreFinder.getDataStore(connectionParameters);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * gets the types of a service.
     * @return the types
     */
    public ArrayList<String> getTypes() {
        if (this.types == null) {
            this.types = new ArrayList();
            try {
                String[] typeNames = this.data.getTypeNames();

                for (String tName : typeNames) {
                    this.types.add(tName);
                }
            } catch (Exception e) {
                //TODO: Be more specific about execptions
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return this.types;
    }

    /**
     * gets the attributes of a tye.
     * @param type type to get attributes of
     * @return the attributes
     */
    public ArrayList<AttributeType> getAttributes(String type) {
        ArrayList<AttributeType> attributes = new ArrayList();
        try {
            SimpleFeatureType schema = this.data.getSchema(type);
            attributes.addAll(schema.getTypes());
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return attributes;
    }

    /**
     * gets the service URL.
     * @return the service URL
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    /**
     * gets the dataStore.
     * @return datastore
     */
    public DataStore getData() {
        return this.data;
    }
}
