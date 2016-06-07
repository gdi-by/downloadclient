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

package de.bayern.gdi.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.CatalogService;
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.services.WFSMeta;
//import de.bayern.gdi.services.WebService;
import de.bayern.gdi.utils.ServiceSetting;
import de.bayern.gdi.utils.StringUtils;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DataBean extends Observable {

    private Stage primaryStage;

    private Map<String, String> namePwMap;
    private ServiceSetting serviceSetting;
    private Map<String, String> staticServices;
    private Map<String, String> catalogServices;
    private ServiceType serviceType;
    private ItemModel dataType;
    private Atom atomService;
    private WFSMeta wfsService;
    private ArrayList<String> serviceTypes;
    private Map<String, String> attributes;
    private String wmsUrl;
    private String wmsName;
    private String userName;
    private String password;


    private CatalogService catalogService;

    /**
     * Constructor.
     */
    public DataBean(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.namePwMap = new HashMap<>();
        this.serviceSetting = new ServiceSetting();
        this.staticServices = this.serviceSetting.getServices();
        this.catalogServices = new HashMap<String, String>();
        this.catalogService = new CatalogService(this.serviceSetting
                .getCatalogueURL());
        this.atomService = null;
        this.wfsService = null;
        this.wmsUrl = this.serviceSetting.getWMSUrl();
        this.wmsName = this.serviceSetting.getWMSName();
        this.userName = null;
        this.password = null;
    }

    /**
     * returns the Name Map.
     * @return the Name Map
     */
    public Map<String, String> getNamePwMap() {
        return namePwMap;
    }

    /**
     * Reset the services list.
     */
    public void reset() {
        this.catalogServices.clear();
    }

    /**
     * Builds a Observable List from the services Map.
     * @return List build from services Map
     */
    public ObservableList<String> getServicesAsList() {
        ObservableList<String> serviceNames =
                FXCollections.observableArrayList();
        for (String name: this.staticServices.keySet()) {
            serviceNames.add(name);
        }
        for (String name: this.catalogServices.keySet()) {
            serviceNames.add(name);
        }
        return serviceNames;
    }

    /**
     * Adds a Service to the list.
     * @param serviceName the Name of the Service
     * @param serviceURL the URL of the Service
     */
    public void addCatalogServiceToList(String serviceName, String serviceURL) {
        this.catalogServices.put(serviceName, serviceURL);
    }

    /**
     * Adds a Service to the list.
     * @param serviceName the Name of the Service
     * @param serviceURL the URL of the Service
     */
    public void addServiceToList(String serviceName, String serviceURL) {
        this.catalogServices.put(serviceName, serviceURL);
    }

    /**
     * Returns the Service URL for a given Service Name.
     * @param serviceName name of a Service
     * @return the url of the service
     */
    public String getServiceURL(String serviceName) {
        String returnStr = null;
        if (this.staticServices.containsKey(serviceName)) {
            returnStr = this.staticServices.get(serviceName);
        }
        if (this.catalogServices.containsKey(serviceName)) {
            returnStr = this.catalogServices.get(serviceName);
        }
        return returnStr;
    }

    /**
     * returns the current stage.
     * @return the stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Set the data type.
     * @param type The data type
     */
    public void setDataType(ItemModel type) {
        this.dataType = type;
    }

    /**
     * Get the data type.
     * @return the datatype
     */
    public ItemModel getDatatype() {
        return dataType;
    }


    /**
     * Set the service type.
     * @param type the service type
     */
    public void setServiceType(ServiceType type) {
        this.serviceType = type;
    }

    /**
     * Get the service type.
     * @return the service type
     */
    public ServiceType getServiceType() {
        return this.serviceType;
    }

    /**
     * gets the Webservice.
     * @return webservice
     */
    public Atom getAtomService() {
        return atomService;
    }

    /**
     * Get the WFS service.
     * @return the service
     */
    public WFSMeta getWFSService() {
        return wfsService;
    }

    /**
     * sets the webservice.
     * @param webService webservice
     */
    public void setAtomService(Atom webService) {
        this.serviceType = ServiceType.Atom;
        this.atomService = webService;
    }

    /**
     * sets the webservice.
     * @param webService webservice
     */
    public void setWFSService(WFSMeta webService) {
        this.serviceType = ServiceType.WFSTwo;
        this.wfsService = webService;
    }

    /**
     * returns true if webservice is set.
     * @return true if webservice ist set; false if not set
     */
    public boolean isWebServiceSet() {
        if (this.atomService == null
            && this.wfsService == null) {
            return false;
        }
        return true;

    }

    /**
     * gets the service Types.
     * @return serviceTypes
     */
    public ArrayList<String> getServiceTypes() {
        return serviceTypes;
    }

    /**
     * sets the service Types.
     * @param serviceTypes service Types
     */
    public void setServiceTypes(ArrayList<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    /**
     * gets the Attributes for a the selected service.
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * sets the Attributes for a selected Service.
     * @param attributes tha attributes
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * Adds an attribute for a selected Service.
     * @param key The key
     * @param value The value
     */
    public void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    /**
     * gets the WMS Url.
     * @return WMS Url
     */
    public String getWmsUrl() {
        return wmsUrl;
    }

    /**
     * sets the WMS Url.
     * @param wmsUrl WMS Url
     */
    public void setWmsUrl(String wmsUrl) {
        this.wmsUrl = wmsUrl;
    }

    /**
     * gets the WMS Name.
     * @return WMS Name
     */
    public String getWmsName() {
        return wmsName;
    }

    /**
     * sets the WMS Name.
     * @param wmsName WMS Name
     */
    public void setWmsName(String wmsName) {
        this.wmsName = wmsName;
    }

    /**
     * sets the Username.
     * @param username the username
     */
    public void setUsername(String username) {
        this.userName = username;
    }

    /**
     * gets the username.
     * @return the username
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * sets the password.
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * gets the password.
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * gets the username and password as base64 encrypted string.
     * @return the base 64 encrypted username and password string
     */
    public String getBase64EncAuth() {
        return StringUtils.getBase64EncAuth(this.userName, this.password);
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }
}
