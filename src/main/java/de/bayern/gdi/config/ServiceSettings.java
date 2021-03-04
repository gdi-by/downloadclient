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

package de.bayern.gdi.config;

import de.bayern.gdi.services.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.xpath.XPathConstants;

import de.bayern.gdi.utils.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class ServiceSettings {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceSettings.class.getName());

    private List<Service> services;
    private Map<String, String> catalogues;
    private Map<String, String> wms;
    private Pattern checkWithGET;
    private String baseDirectory;

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
        Optional<String> v = this.getCatalogues()
            .values()
            .stream()
            .findFirst();
        return v.isPresent() ? v.get() : null;
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
            LOG.error(e.getMessage(), e);
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

    public String getBaseDirectory() {
        return this.baseDirectory;
    }

    private void parseDocument(Document xmlDocument) throws IOException {
        this.services = parseService(xmlDocument);
        this.catalogues = parseNameURLScheme(xmlDocument, "catalogues");
        this.checkWithGET = extractCheckWithGET(xmlDocument);
        this.wms = parseSchema(xmlDocument,
                "wms",
                "url",
                "layer",
                "name",
                "source");
        this.baseDirectory = parseBaseDirectory(xmlDocument);
    }

    /**
     * gets the name of the configuration.
     * @return name of the config
     */
    public static String getName() {
        return NAME;
    }

    /**
     * Check if an URL should be check with HTTP GET instead of HTTP HEAD
     * for being access restricted.
     * @param url The URL to check.
     * @return true if the URL needs a GET check otherwise false.
     */
    public boolean checkRestrictionWithGET(String url) {
        return this.checkWithGET != null
            && this.checkWithGET.matcher(url).matches();
    }

    // This is an XPATH expression and not an URI.
    @java.lang.SuppressWarnings("squid:S1075")
    private static final String CHECK_WITH_GET_XPATH =
        "//check-restriction/use-get-url/text()";

    private Pattern extractCheckWithGET(Document doc)
        throws IOException {

        StringBuilder pattern = new StringBuilder();

        NodeList nodes = (NodeList) XML.xpath(
            doc, CHECK_WITH_GET_XPATH,
            XPathConstants.NODESET, null, null);

        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }

        // Compile them into a super pattern.
        for (int i = 0, n = nodes.getLength(); i < n; i++) {
            if (i > 0) {
                pattern.append('|');
            }
            String expr = nodes.item(i).getTextContent().trim();
            pattern.append('(').append(expr).append(')');
        }
        try {
            return Pattern.compile(pattern.toString());
        } catch (PatternSyntaxException pse) {
            throw new IOException(pse);
        }
    }

    private static final String SERVICE_XPATH =
        "//*[local-name() = $NODE]/service/*[local-name() = $NAME]/text()";

    private static final String BASEDIR_XPATH = "//basedir/text()";

    private Map<String, String> parseSchema(Document xmlDocument, String
            nodeName, String... names) throws IOException {
        Map<String, String> map = new HashMap<>();

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
        List<Service> servicesList = new ArrayList<>();

        NodeList servicesNL = xmlDocument.getElementsByTagName("services");
        if (servicesNL.getLength() == 0) {
            return servicesList;
        }

        Node servicesNode = servicesNL.item(0);
        NodeList serviceNL = servicesNode.getChildNodes();

        for (int i = 0; i < serviceNL.getLength(); i++) {
            Node serviceNode = serviceNL.item(i);
            if (serviceNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList serviceValueNL = serviceNode.getChildNodes();
                String serviceURL = null;
                String serviceName = null;
                boolean restricted = false;
                for (int k = 0; k < serviceValueNL.getLength(); k++) {
                    Node serviceValueNode = serviceValueNL.item(k);
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
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
        return servicesList;
    }

    private Map<String, String> parseNameURLScheme(Document xmlDocument,
                                                   String nodeName)
            throws IOException {
        Map<String, String> servicesMap = new HashMap<>();

        NodeList servicesNL = xmlDocument.getElementsByTagName(nodeName);
        Node servicesNode = servicesNL.item(0);
        if (servicesNode == null) {
            throw new IOException(nodeName + " Node not found - Config broken");
        }
        NodeList serviceNL = servicesNode.getChildNodes();

        for (int i = 0; i < serviceNL.getLength(); i++) {
            Node serviceNode = serviceNL.item(i);
            if (serviceNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList serviceValueNL = serviceNode.getChildNodes();
                String serviceURL = null;
                String serviceName = null;
                for (int k = 0; k < serviceValueNL.getLength(); k++) {
                    Node serviceValueNode = serviceValueNL.item(k);
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
            throw new IOException(nodeName + " seems to be empty - Config "
                    + "broken");
        }
        return servicesMap;
    }

    private String parseBaseDirectory(Document xmlDocument) {
        return (String) XML.xpath(
            xmlDocument, BASEDIR_XPATH, XPathConstants.STRING, null);
    }

    @Override
    public String toString() {
        return "ServiceSettings: {"
            + "services=" + services
            + ", catalogues=" + catalogues
            + ", wms=" + wms
            + ", checkWithGET=" + checkWithGET
            + ", basedir=" + baseDirectory
            + "}";
    }
}
