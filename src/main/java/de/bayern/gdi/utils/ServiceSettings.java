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

package de.bayern.gdi.utils;

import de.bayern.gdi.services.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class ServiceSettings {

    private static final Logger log
        = Logger.getLogger(ServiceSettings.class.getName());

    private List<Service> services;
    private Map<String, String> catalogues;
    private Map<String, String> wms;

    private static final String NAME =
            "ServiceSetting";

    public ServiceSettings(Document doc) throws IOException {
        parseDocument(doc);
    }

    /**
     * gets the Catalogue URL as String.
     * @return catalogue URL as String
     */
    public String getCatalogue() {
        for (String value: this.getCatalogues().values()) {
            return value;
        }
        return null;
    }

    /**
     * gets the catalogue URL.
     * @return The catalogue URL
     */
    public URL getCatalogueURL() {
        try {
            String cat = getCatalogue();
            return cat != null ? new URL(cat) : null;
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
    /**
     * returns a map of Strings with service Names und URLS.
     * @return Map of Strings with <Name, URL> of Services
     */
    public List<Service> getServices()  {
        return this.services;
    }

    /**
     * returns a map of Strings with catalogue Names and URLS.
     * @return Map of Strings with <Name, URL> of Catalogs
     */
    private Map<String, String> getCatalogues() {
        return this.catalogues;
    }

    /**
     * gets the WMS Url.
     * @return the WMS url
     */
    public String getWMSUrl() {
        return this.wms.get("url");
    }

    /**
     * gets the WMS Name.
     * @return the WMS Name
     */
    public String getWMSName() {
        return this.wms.get("name");
    }


    /**
     * gets the WMS Name.
     * @return the WMS Name
     */
    public String getWMSSource() {
        return this.wms.get("source");
    }

    /**
     * gets the WMS Name.
     * @return the WMS Name
     */
    public String getWMSLayer() {
        return this.wms.get("layer");
    }


    private void parseDocument(Document xmlDocument) throws IOException {
        this.services = parseService(xmlDocument);
        this.catalogues = parseNameURLScheme(xmlDocument, "catalogues");
        this.wms = parseSchema(xmlDocument,
                "wms",
                "url",
                "layer",
                "name",
                "source");
    }

    /**
     * gets the name of the configuration.
     * @return name of the config
     */
    public static String getName() {
        return NAME;
    }

    /**Parse Node by name, save all elements to map.*/
    private Map<String, String> parseNodeForElements(Document doc,
            String nodeName) throws IOException {
        Node parent = doc.getElementsByTagName(nodeName).item(0);
        if (parent == null) {
            throw new IOException("Node " + nodeName + " not found");
        }

        Map<String, String> elements = new HashMap<String, String>();

        NodeList childs = parent.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elements.put(node.getNodeName(), node.getTextContent());
            }
        }
        return elements;
    }

    private static final String SERVICE_XPATH =
        "//*[local-name() = $NODE]/service/*[local-name() = $NAME]/text()";

    private Map<String, String> parseSchema(Document xmlDocument, String
            nodeName, String... names) throws IOException {
        Map<String, String> map = new HashMap<String, String>();

        HashMap<String, String> vars = new HashMap<>();
        vars.put("NODE", nodeName);

        for (String name: names) {
            vars.put("NAME", name);
            String value = (String) XML.xpath(
                xmlDocument, SERVICE_XPATH, XPathConstants.STRING, null, vars);
            if (value == null || value.isEmpty()) {
                throw new IOException(name + " in " + nodeName + " Node not "
                        + "Found - Config broken");
            }
            map.put(name, value);
        }
        return map;
    }

    private List<Service> parseService(Document xmlDocument) {
        List<Service> servicesList = new ArrayList<Service>();

        NodeList servicesNL = xmlDocument.getElementsByTagName("services");
        Node servicesNode = servicesNL.item(0);
        NodeList serviceNL = servicesNode.getChildNodes();
        Node serviceNode, serviceValueNode;

        for (int i = 0; i < serviceNL.getLength(); i++) {
            serviceNode = serviceNL.item(i);
            if (serviceNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList serviceValueNL = serviceNode.getChildNodes();
                String serviceURL = null;
                String serviceName = null;
                boolean restricted = false;
                for (int k = 0; k < serviceValueNL.getLength(); k++) {
                    serviceValueNode = serviceValueNL.item(k);
                    if (serviceValueNode.getNodeType() == 1) {
                        if (serviceValueNode.getNodeName().equals("url")) {
                            serviceURL =
                             serviceValueNode.getFirstChild().getTextContent();
                        } else if (serviceValueNode.getNodeName()
                                .equals("name")) {
                            serviceName =
                             serviceValueNode.getFirstChild().getTextContent();
                        } else if (serviceValueNode.getNodeName()
                                .equals("restricted")) {
                            String restr =
                             serviceValueNode.getFirstChild().getTextContent();
                            restricted = restr.equals("true");
                        }
                    }
                }
                if (serviceURL != null && serviceName != null) {
                    try {
                        Service service = new Service(new URL(serviceURL),
                                serviceName,
                                restricted);
                        servicesList.add(service);
                    } catch (MalformedURLException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
        return servicesList;
    }

    private Map<String, String> parseNameURLScheme(Document xmlDocument,
                                                   String nodeName)
            throws IOException {
        Map<String, String> servicesMap = new HashMap<String, String>();

        NodeList servicesNL = xmlDocument.getElementsByTagName(nodeName);
        Node servicesNode = servicesNL.item(0);
        if (servicesNode == null) {
            throw new IOException(nodeName + " Node not found - Config broken");
        }
        NodeList serviceNL = servicesNode.getChildNodes();
        Node serviceNode, serviceValueNode;

        for (int i = 0; i < serviceNL.getLength(); i++) {
            serviceNode = serviceNL.item(i);
            if (serviceNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList serviceValueNL = serviceNode.getChildNodes();
                String serviceURL = null;
                String serviceName = null;
                for (int k = 0; k < serviceValueNL.getLength(); k++) {
                    serviceValueNode = serviceValueNL.item(k);
                    if (serviceValueNode.getNodeType() == 1) {
                        if (serviceValueNode.getNodeName().equals("url")) {
                            serviceURL =
                             serviceValueNode.getFirstChild().getTextContent();
                        } else if (serviceValueNode.getNodeName()
                                .equals("name")) {
                            serviceName =
                             serviceValueNode.getFirstChild().getTextContent();
                        }
                    }
                }
                if (serviceURL != null && serviceName != null) {
                    servicesMap.put(serviceName, serviceURL);
                }
            }
        }
        if (servicesMap.isEmpty()) {
            throw new IOException(nodeName + " seeems to be empty - Config "
                    + "broken");
        }
        return servicesMap;
    }
}
