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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

import de.bayern.gdi.utils.StringUtils;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class WFSOne extends WebService {

    private static final Logger log
            = Logger.getLogger(WFSOne.class.getName());

    private ArrayList<String> types;
    private DataStore data;

    private String userName;
    private String password;

    /**
     * Constructor.
     * @param serviceURL the service URL
     * @param userName username if authentication is needed, else null
     * @param password password if auth is needed, else null
     */
    public WFSOne(String serviceURL, String userName, String password) {
        this.serviceURL = serviceURL;

        this.userName = userName;
        this.password = password;

        Map<String, String> connectionParameters = new HashMap<>();

        if (StringUtils.getBase64EncAuth(
            this.userName, this.password) != null) {
            connectionParameters.put(
                    "Authorization", "Basic "
                            + StringUtils.getBase64EncAuth(
                            this.userName, this.password));
        }
        connectionParameters.put(
                "WFSDataStoreFactory:GET_CAPABILITIES_URL", this.serviceURL);

        try {
            this.data = DataStoreFinder.getDataStore(connectionParameters);
            this.types = new ArrayList<String>();
            String[] typeNames = this.data.getTypeNames();

            for (String tName : typeNames) {
                this.types.add(tName);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * gets the types of a service.
     * @return the types
     */
    public ArrayList<String> getTypes() {
        return this.types;
    }

    /**
     * gets the attributes of a tye.
     * @param type type to get attributes of
     * @return the attributes
     */
    public Map<String, String> getAttributes(String type) {
        ArrayList<AttributeType> attributes = new ArrayList<>();
        Map<String, String> map = new HashMap<String, String>();
        try {
            SimpleFeatureType schema = this.data.getSchema(type);
            attributes.addAll(schema.getTypes());
            for (AttributeType attribute: attributes) {
                map.put(attribute.getName().toString(),
                        attribute.getBinding().toString());
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return map;
    }

    /**
     * gets the dataStore.
     * @return datastore
     */
    public DataStore getData() {
        return this.data;
    }

    /**
     * @inheritDoc
     * @return the ServiceType
     */
    public ServiceType getServiceType() {
        return ServiceType.WFSOne;
    }
}
