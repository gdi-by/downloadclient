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

import de.bayern.gdi.model.DownloadStep;
import de.bayern.gdi.model.Parameter;
import de.bayern.gdi.model.ProcessingStep;
import de.bayern.gdi.services.Atom;
import de.bayern.gdi.services.CatalogService;
import de.bayern.gdi.services.Service;
import de.bayern.gdi.services.ServiceType;
import de.bayern.gdi.services.WFSMeta;
import de.bayern.gdi.config.Config;
import de.bayern.gdi.utils.ServiceChecker;
import de.bayern.gdi.config.ServiceSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static de.bayern.gdi.gui.FeatureModel.FilterType.FILTER;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class DataBean extends Observable {

    private static final String FEATURE_TYPE_NAME_SUFFIX
        = " (Filter)";

    private Map<String, String> namePwMap;
    private List<Service> staticServices;
    private List<Service> catalogServices;
    private ItemModel dataType;
    private Atom atomService;
    private WFSMeta wfsService;
    private List<Attribute> attributes;
    private List<ProcessingStep> processingSteps;
    private Service selectedService;

    private CatalogService catalogService;

    private String featureTypeModel;

    /**
     * Attribute representation.
     */
    public static final class Attribute {
        /** name. */
        private String name;

        /** value. */
        private String value;

        /** type. */
        private String type;

        /**
         * Returns the name of the attribute.
         * @return the name of the attribute
         */
        public String getName() {
            return this.name;
        }

        /**
         * Returns the value of the attribute.
         * @return the value of the attribute
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Returns the type of the attribute.
         * @return the type of the attribute.
         */
        public String getType() {
            return this.type;
        }

        /**
         * Constructor.
         * @param name the name
         * @param value the value
         * @param type the type of the value
         */
        public Attribute(String name, String value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }
    }

    /**
     * Constructor.
     */
    public DataBean() throws IOException {
        this.namePwMap = new HashMap<>();

        ServiceSettings serviceSetting = Config.getInstance().getServices();

        this.staticServices = serviceSetting.getServices();

        this.attributes = new ArrayList<>();

        this.catalogServices = new ArrayList<>();
        this.featureTypeModel = "WFS2_Basic";

        if (ServiceChecker.isReachable(serviceSetting.getCatalogueURL())) {
            try {
                this.catalogService =
                        new CatalogService(serviceSetting.getCatalogueURL());
            } catch (URISyntaxException | IOException e) {
                throw new IOException(
                        "Failed to Initialize Catalog Service: '"
                                + serviceSetting.getCatalogueURL()
                                + "'");
            }
        }
        this.processingSteps = new ArrayList<>();
    }

    /**
     * sets the selected service.
     * @param smi the selected service
     */
    public void setSelectedService(Service smi) {
        this.selectedService = smi;
    }

    /**
     * gets the selected service.
     * @return the selected service
     */
    public Service getSelectedService() {
        return this.selectedService;
    }

    /**
     * returns the Name Map.
     * @return the Name Map
     */
    public Map<String, String> getNamePwMap() {
        return namePwMap;
    }

   /**
    * Reset the catalog services list.
    */
    public void resetCatalogLists() {
        this.catalogServices.clear();
    }

   /**
    * Reset the selected service.
    */
    public void resetSelectedService() {
        this.atomService = null;
        this.wfsService = null;
        this.namePwMap.clear();
        this.dataType = null;
        this.attributes.clear();
    }

    /**
     * Reset the services list.
     */
    public void reset() {
        this.catalogServices.clear();
        this.processingSteps.clear();
        this.attributes.clear();
        this.dataType = null;
        this.atomService = null;
        this.wfsService = null;
        this.namePwMap.clear();
    }

    /**
     * Builds a Observable List from the services Map.
     * @return List build from services Map
     */
    public ObservableList<ServiceModel> getServicesAsList() {
        List<Service> all = new ArrayList<>();
        all.addAll(this.staticServices);
        all.addAll(this.catalogServices);
        List<ServiceModel> allModels = new ArrayList<>();
        for (Service entry: all) {
            ServiceModel sm = new ServiceModel(entry);
            allModels.add(sm);
        }
        return FXCollections.observableArrayList(allModels);
    }

    /**
     * Adds a Service to the list.
     * @param service the Service
     */
    public void addCatalogServiceToList(Service service) {
        this.catalogServices.add(service);
    }

    /**
     * Adds a Service to the list.
     * @param service the Service
     */
    public void addServiceToList(Service service) {
        this.catalogServices.add(service);
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
     * Get the service type.
     * @return the service type
     */
    public ServiceType getServiceType() {
        return this.selectedService.getServiceType();
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
        this.atomService = webService;
    }

    /**
     * sets the webservice.
     * @param webService webservice
     */
    public void setWFSService(WFSMeta webService) {
        this.wfsService = webService;
    }

    /**
     * returns true if webservice is set.
     * @return true if webservice ist set; false if not set
     */
    public boolean isWebServiceSet() {
        return this.atomService != null || this.wfsService != null;
    }

    /**
     * gets the Attributes for a the selected service.
     * @return the attributes
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * sets the Attributes for a selected Service.
     * @param attributes tha attributes
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Adds an attribute for a selected Service.
     * @param key The key
     * @param value The value
     * @param type the type of the value
     */
    public void addAttribute(String key, String value, String type) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<>();
        }
        for (Attribute attr: this.attributes) {
            if (attr.name.equals(key)) {
                attr.value = value;
                attr.type = type;
                return;
            }
        }
        Attribute attr = new Attribute(key, value, type);
        this.attributes.add(attr);
    }

    /**
     * gets the attributevlaue by key.
     * @param key the key
     * @return the value for the key
     */
    public String getAttributeValue(String key) {
        for (Attribute attr: this.attributes) {
            if (attr.name.equals(key)) {
                return attr.value;
            }
        }
        return null;
    }

    /**
     * @return the processingSteps
     */
    public List<ProcessingStep> getProcessingSteps() {
        return processingSteps;
    }

    /**
     * @param processingSteps the processingSteps to set
     */
    public void setProcessingSteps(List<ProcessingStep> processingSteps) {
        this.processingSteps = processingSteps;
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }

    /**
     * @return The current feature type model
     */
    public String getFeatureTypeModel() {
        return featureTypeModel;
    }

    /**
     * Set the current feature type model.
     * @param featureTypeModel the new model.
     */
    public void setFeatureTypeModel(String featureTypeModel) {
        this.featureTypeModel = featureTypeModel;
    }

    /**
     * gets Downloadstep from Frontend.
     * @param savePath the save path
     * @return downloadStep
     */
    public DownloadStep convertToDownloadStep(String savePath) {
        ServiceType type = getServiceType();
        String serviceURL = type == ServiceType.ATOM
            ? getAtomService().getURL()
            : getWFSService().getURL();

        List<Attribute> attrs = getAttributes();
        ArrayList<Parameter> parameters = new ArrayList<>(attrs.size());
        for (Attribute attr: attrs) {
            Parameter param = new Parameter(attr.name, attr.value);
            parameters.add(param);
        }
        String serviceTypeStr = null;
        switch (type) {
            case WFS_ONE:
                serviceTypeStr = "WFS1";
                break;
            case WFS_TWO:
                ItemModel itemModel = getDatatype();
                if (itemModel instanceof StoredQueryModel) {
                    serviceTypeStr = "WFS2_SIMPLE";
                } else if (isFilterType()) {
                    serviceTypeStr = "WFS2_SQL";
                } else {
                    serviceTypeStr = "WFS2_BASIC";
                }
                break;
            case ATOM:
                serviceTypeStr = "ATOM";
                break;
            default:
        }
        return new DownloadStep(
            getDatatype().getDataset(),
            parameters,
            serviceTypeStr,
            serviceURL,
            savePath,
            processingSteps);
    }

    /**
     * @return <code>true</code> if it is a CQLFilter request,
     * <code>false</code> otherwise
     */
    public boolean isFilterType() {
        if (dataType != null && dataType instanceof OverallFeatureTypeModel) {
            return true;
        }
        if (dataType != null && dataType instanceof FeatureModel) {
            return FILTER.equals(((FeatureModel) dataType).getFilterType());
        }
        return false;
    }

    /**
     * @return <code>true</code> if it is a CQLFilter request over
     * all feature types, <code>false</code> otherwise
     */
    public boolean isMultipleQuery() {
        if (dataType != null && dataType instanceof OverallFeatureTypeModel) {
            return true;
        }
        return false;
    }

}
