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

import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class ServiceSetting {

    private static final Logger log
        = Logger.getLogger(ServiceSetting.class.getName());

    private InputStream settingStream;
    private Map<String, String> services;
    private Document xmlSettingFile;
    private static final String SERVICE_SETTING_FILEPATH =
            "serviceSetting.xml";
    private Map<String, String> catalogues;
    private Map<String, String> wms;

    /**
     * Constructor.
     */
    public ServiceSetting() {
        this(SERVICE_SETTING_FILEPATH);
    }

    /**
     * Constructor.
     * @param filePath Path the the serviceSettings.xml
     */
    public ServiceSetting(String filePath) {
        this.settingStream = getFileStream(filePath);
        this.xmlSettingFile = XML.getDocument(this.settingStream);
        parseDocument(this.xmlSettingFile);
    }

    /**
     * gets the Catalogue URL as String.
     * @return catalogue URL as String
     */
    public String getCatalogue() {
        Iterator it = this.getCatalogues().entrySet().iterator();
        Map.Entry pair = (Map.Entry)it.next();
        return (String) pair.getValue();

    }

    /**
     * gets teh catalogue URL.
     * @return The catalogue URL
     */
    public URL getCatalogueURL() {
        URL url = null;
        try {
            url = new URL(getCatalogue());
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return url;
    }
    /**
     * returns a map of Strings with service Names und URLS.
     * @return Map of Strings with <Name, URL> of Services
     */
    public Map<String, String> getServices()  {
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
        Iterator it = this.wms.entrySet().iterator();
        Map.Entry pair = (Map.Entry)it.next();
        return (String) pair.getValue();
    }

    /**
     * gets the WMS Name.
     * @return the WMS Name
     */
    public String getWMSName() {
        Iterator it = this.wms.entrySet().iterator();
        Map.Entry pair = (Map.Entry)it.next();
        return (String) pair.getKey();
    }

    private void parseDocument(Document xmlDocument) {
        this.services = parseNameURLScheme(xmlDocument, "services");
        this.catalogues = parseNameURLScheme(xmlDocument, "catalogues");
        this.wms = parseNameURLScheme(xmlDocument, "wms");
    }

    private Map<String, String> parseNameURLScheme(Document xmlDocument,
                                                   String nodeName) {
        Map<String, String> servicesMap = new HashMap<String, String>();

        NodeList servicesNL = xmlDocument.getElementsByTagName(nodeName);
        Node servicesNode = servicesNL.item(0);
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
        return servicesMap;
    }

    private static InputStream getFileStream(String fileName) {

        ClassLoader classLoader = ServiceSetting.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(fileName);

        return stream;
    }

}
