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

package de.bayern.gdi.experimental;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class ServiceSetting {

    private File settingFile = null;
    private Map<String, String> services;
    private Document xmlSettingFile = null;
    private static final String SERVICE_SETTING_FILEPATH =
            "serviceSetting.xml";

    /**
     * @brief Constructor
     */
    public ServiceSetting() {
        this(SERVICE_SETTING_FILEPATH);
    }

    /**
     * @brief Constructor
     * @param filePath Path the the serviceSettings.xml
     */
    public ServiceSetting(String filePath) {
        this.settingFile = getFile(filePath);
        this.xmlSettingFile = getXMLDocument(this.settingFile);
        this.services = parseXML(this.xmlSettingFile);
    }

    /**
     * @breif returns a map of Strings with service Names und URLS
     * @return Map of Strings with <Name, URL> of Services
     */
    public Map<String, String> getServices()  {
        return this.services;
    }

    private Map<String, String> parseXML(Document xmlDocument) {
        Map<String, String> servicesMap = new HashMap<String, String>();

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
                for (int k = 0; k < serviceValueNL.getLength(); k++) {
                    serviceValueNode = serviceValueNL.item(k);
                    if (serviceValueNode.getNodeType() == 1) {
                        if (serviceValueNode.getNodeName() == "url") {
                            serviceURL =
                             serviceValueNode.getFirstChild().getTextContent();
                        } else if (serviceValueNode.getNodeName() == "name") {
                            serviceName =
                             serviceValueNode.getFirstChild().getTextContent();
                        }
                    }
                }
                servicesMap.put(serviceName, serviceURL);
            }
        }
        return servicesMap;
    }

    private Document getXMLDocument(File fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(fileName);
        } catch (Exception e) {
            // TODO: Add logging.
            System.err.println(e);
        }
        return document;
    }

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file;

    }
}
