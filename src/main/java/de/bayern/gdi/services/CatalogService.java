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

import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class CatalogService {

    private static final String CSW_NAMESPACE =
            "http://www.opengis.net/cat/csw/2.0.2";
    private static final String GMD_NAMESPACE =
            "http://www.isotc211.org/2005/gmd";
    private static final String OWS_NAMESPACE =
            "http://www.opengis.net/ows";
    private static final String SRV_NAMESPACE =
            "http://www.isotc211.org/2005/srv";
    private static final String GCO_NAMESPACE =
            "http://www.isotc211.org/2005/gco";
    private static final String CSW_QUERY_FILEPATH =
            "csw_searchQuery.xml";
    private static final Logger LOG = LoggerFactory.getLogger(CatalogService.class.getName());
    private URL catalogURL;
    private String userName;
    private static final int MIN_SEARCHLENGTH = 2;
    private String password;
    private String providerName;
    private NamespaceContext context;
    private URL getRecordsURL;
    private static final String GMD_IDENTIFICATION_INFO
            = "gmd:identificationInfo";
    private static final String SRV_SV_SERVICE_IDENTIFICATION
            = "/srv:SV_ServiceIdentification";
    private static final String GCO_CHARACTER_STRING
            = "/gco:CharacterString";
    private static final String GMD_LINKAGE
            = "/gmd:linkage";
    private static final String GMD_URL
            = "/gmd:URL";
    private static final String SV_OPERATION_METADATA
            = "srv:SV_OperationMetadata";

    /**
     * Constructor.
     *
     * @param url      String URL
     * @param userName Username
     * @param password Password
     * @throws URISyntaxException if URL is wrong
     * @throws IOException if something in IO is wrong
     */
    public CatalogService(String url, String userName, String password)
            throws URISyntaxException, IOException {
        this(new URL(url), userName, password);
    }

    /**
     * Constructor.
     *
     * @param url Stirng URL
     * @throws URISyntaxException if URL is wrong
     * @throws IOException if something in IO is wrong
     */
    public CatalogService(String url) throws URISyntaxException, IOException {
        this(url, null, null);
    }

    /**
     * Constructor.
     *
     * @param url URL
     * @throws URISyntaxException if URL is wrong
     * @throws IOException if something in IO is wrong
     */
    public CatalogService(URL url)
            throws URISyntaxException, IOException {
        this(url, null, null);
    }

    /**
     * Constructor.
     *
     * @param url      URL
     * @param userName Username
     * @param password Password
     * @throws URISyntaxException if URL is wrong
     * @throws IOException if something in IO is wrong
     */
    public CatalogService(URL url, String userName, String password)
    throws URISyntaxException, IOException {
        this.catalogURL = url;
        this.userName = userName;
        this.password = password;
        this.context = new NamespaceContextMap(
                "csw", CSW_NAMESPACE,
                "gmd", GMD_NAMESPACE,
                "ows", OWS_NAMESPACE,
                "srv", SRV_NAMESPACE,
                "gco", GCO_NAMESPACE);
        Document xml = XML.getDocument(this.catalogURL,
                    this.userName,
                    this.password);
        if (xml != null) {
            String getProviderExpr = "//ows:ServiceIdentification/ows:Title";
            Node providerNameNode = (Node) XML.xpath(xml,
                    getProviderExpr,
                    XPathConstants.NODE,
                    context);
            this.providerName = providerNameNode.getTextContent();
            String getRecordsURLExpr = "//ows:OperationsMetadata"
                    + "/ows:Operation[@name='GetRecords']"
                    + "/ows:DCP"
                    + "/ows:HTTP"
                    + "/ows:Post";

            NodeList rL = (NodeList) XML.xpath(xml,
                    getRecordsURLExpr,
                    XPathConstants.NODESET,
                    context);

            for (int i = 0; i < rL.getLength(); i++) {
                Node gruNode = rL.item(i);
                String getRecordsValueStr = (String) XML.xpath(gruNode,
                        "ows:Constraint/ows:Value/text()",
                        XPathConstants.STRING,
                        this.context);

                if (getRecordsValueStr == null
                        || getRecordsValueStr.isEmpty()
                        || getRecordsValueStr.equals("XML")) {

                    String getRecordsURLStr = (String) XML.xpath(gruNode,
                            "@*[name()='xlink:href']",
                            XPathConstants.STRING,
                            this.context);

                    this.getRecordsURL = null;
                    this.getRecordsURL = new URL(getRecordsURLStr);
                    break;
                }
            }
        }
    }

    /**
     * gets the Name of the service Provider.
     *
     * @return Name of the service Provider
     */
    public String getProviderName() {
        return this.providerName;
    }

    /**
     * retrieves a Map of ServiceNames and URLs for a Filter-Word.
     *
     * @param filter the Word to filter to
     * @return Map of Service Names and URLs
     * @throws URISyntaxException if URL is wrong
     * @throws IOException if something in IO is wrong
     */
    public List<Service> getServicesByFilter(String filter)
        throws URISyntaxException, IOException {
        List<Service> services = new ArrayList<>();
        if (filter.length() > MIN_SEARCHLENGTH && this.getRecordsURL != null) {
            String search = loadXMLFilter(filter);
            Document xml = XML.getDocument(this.getRecordsURL,
                        this.userName,
                        this.password,
                        search,
                        true);
            Node exceptionNode = (Node) XML.xpath(xml,
                    "//ows:ExceptionReport",
                    XPathConstants.NODE, this.context);
            if (exceptionNode != null) {
                String exceptionCode = (String) XML.xpath(xml,
                        "//ows:ExceptionReport/ows:Exception/@exceptionCode",
                        XPathConstants.STRING, this.context);
                String exceptionlocator = (String) XML.xpath(xml,
                        "//ows:ExceptionReport/ows:Exception/@locator",
                        XPathConstants.STRING, this.context);
                String exceptiontext = (String) XML.xpath(xml,
                        "//ows:ExceptionReport/ows:Exception/ows:ExceptionText",
                        XPathConstants.STRING, this.context);
                String excpetion = "An Excpetion was thrown by the CSW: \n";
                excpetion += "\texceptionCode: " + exceptionCode + "\n";
                excpetion += "\tlocator: " + exceptionlocator + "\n";
                excpetion += "\texceptiontext: " + exceptiontext + "\n";
                LOG.error(excpetion, xml);
                return services;
            }
            String nodeListOfServicesExpr =
                    "//csw:SearchResults//gmd:MD_Metadata";
            NodeList servicesNL = (NodeList) XML.xpath(xml,
                    nodeListOfServicesExpr,
                    XPathConstants.NODESET, this.context);
            services = parseServices(servicesNL);
        }
        return services;
    }

    private String makeCapabiltiesURL(String url, String type) {
        if (url.endsWith("?") && type.toUpperCase().contains("WFS")) {
            return url + "service=wfs"
                    + "&acceptversions=" + getVersionOfType(type)
                    + "&request=GetCapabilities";
        }
        if (!url.toUpperCase().contains("GETCAPABILITIES") && type
                .toUpperCase().contains("WFS")) {
            return url + "?service=wfs"
                    + "&acceptversions=" + getVersionOfType(type)
                    + "&request=GetCapabilities";
        }
        return url;
    }

    private String getVersionOfType(String type) {
        String versionNumber = type.substring(type.lastIndexOf(' ') + 1);
        if ("2.0".equals(versionNumber)) {
            versionNumber = "2.0.0";
        } else if ("1.0".equals(versionNumber)) {
            versionNumber = "1.0.0";
        } else if ("1.1".equals(versionNumber)) {
            versionNumber = "1.1.0";
        }
        return versionNumber;
    }

    /**
     * parses the services.
     * @param servicesNL node list of service entries
     * @return list of services
     * @throws MalformedURLException if url is wrong
     */
    public List<Service> parseServices(NodeList servicesNL)
            throws MalformedURLException {
        List<Service> services = new ArrayList<>();
        for (int i = 0; i < servicesNL.getLength(); i++) {
            Node serviceN = servicesNL.item(i);
            String nameExpr =
                    GMD_IDENTIFICATION_INFO
                            + SRV_SV_SERVICE_IDENTIFICATION
                            + "/gmd:citation"
                            + "/gmd:CI_Citation"
                            + "/gmd:title"
                            + GCO_CHARACTER_STRING;
            String serviceName = (String) XML.xpath(serviceN,
                    nameExpr,
                    XPathConstants.STRING, context);
            String restrictionExpr =
                    GMD_IDENTIFICATION_INFO
                            + SRV_SV_SERVICE_IDENTIFICATION
                            + "/gmd:resourceConstraints"
                            + "/gmd:MD_SecurityConstraints"
                            + "/gmd:classification"
                            + "/gmd:MD_ClassificationCode";
            String restriction = (String) XML.xpath(serviceN,
                    restrictionExpr,
                    XPathConstants.STRING, context);
            boolean restricted = false;
            if ("restricted".equals(restriction)) {
                restricted = true;
            }
            String serviceTypeVersionExpr =
                    GMD_IDENTIFICATION_INFO
                            + SRV_SV_SERVICE_IDENTIFICATION
                            + "/srv:serviceTypeVersion"
                            + GCO_CHARACTER_STRING;
            String serviceTypeVersion = (String) XML.xpath(serviceN,
                    serviceTypeVersionExpr,
                    XPathConstants.STRING, context);

            String serviceURL = getServiceURL(serviceN);

            if (serviceTypeVersion.isEmpty()) {
                serviceTypeVersion = "ATOM";
            }
            if (!serviceName.isEmpty() && serviceURL != null) {
                serviceURL = makeCapabiltiesURL(serviceURL,
                        serviceTypeVersion);
                Service service = new Service(new URL(serviceURL),
                        serviceName,
                        restricted,
                        ServiceType.guess(serviceTypeVersion));
                services.add(service);
            }
        }
        return services;
    }

    private String getServiceURL(Node serviceNode) {
        String onLineExpr = "gmd:distributionInfo"
                + "/gmd:MD_Distribution"
                + "/gmd:transferOptions"
                + "/gmd:MD_DigitalTransferOptions"
                + "/gmd:onLine";
        NodeList onlineNL = (NodeList) XML.xpath(serviceNode,
                onLineExpr,
                XPathConstants.NODESET, context);
        for (int j = 0; j < onlineNL.getLength(); j++) {
            Node onlineN = onlineNL.item(j);
            String urlExpr =
                    "gmd:CI_OnlineResource"
                            + GMD_LINKAGE
                            + GMD_URL;
            String onLineserviceURL = (String) XML.xpath(onlineN,
                    urlExpr,
                    XPathConstants.STRING, context);
            String applicationprofileExpr =
                    "gmd:CI_OnlineResource"
                            + "/gmd:applicationProfile"
                            + GCO_CHARACTER_STRING;
            String applicationProfile = (String) XML.xpath(onlineN,
                    applicationprofileExpr,
                    XPathConstants.STRING, context);
            applicationProfile = applicationProfile.toLowerCase();

            if (applicationProfile.equals("wfs-url")
            || applicationProfile.equals("feed-url")
            || applicationProfile.equals("dienste-url")
            || applicationProfile.equals("download")) {
                return onLineserviceURL;
            }
        }
        String operationMetaDataExpr =
                GMD_IDENTIFICATION_INFO
                        + SRV_SV_SERVICE_IDENTIFICATION
                        + "/srv:containsOperations";
        NodeList operationsMetadataNL = (NodeList) XML.xpath(serviceNode,
                operationMetaDataExpr,
                XPathConstants.NODESET, context);
        Node firstNode = null;
        for (int j = 0; j < operationsMetadataNL.getLength(); j++) {
            Node operationMetadataNode = operationsMetadataNL.item(j);
            if (j == 0) {
                firstNode = operationMetadataNode;
            }
            String operationsNameExpr =
                    SV_OPERATION_METADATA
                            + "/srv:operationName"
                            + GCO_CHARACTER_STRING;
            String applicationProfile = (String) XML.xpath(
                    operationMetadataNode,
                    operationsNameExpr,
                    XPathConstants.STRING, context);

            if (applicationProfile.equalsIgnoreCase("getcapabilities")) {
                String operationsURLExpr =
                        SV_OPERATION_METADATA
                                + "/srv:connectPoint"
                                + "/gmd:CI_OnlineResource"
                                + GMD_LINKAGE
                                + GMD_URL;
                String operationsURL = (String) XML.xpath(
                        operationMetadataNode,
                        operationsURLExpr,
                        XPathConstants.STRING, context);
                if (!operationsURL.isEmpty()) {
                    return operationsURL;
                }
            }
        }
        if (firstNode != null) {
            String operationsURLExpr =
                    SV_OPERATION_METADATA
                            + "/srv:connectPoint"
                            + "/gmd:CI_OnlineResource"
                            + GMD_LINKAGE
                            + GMD_URL;
            return (String)XML.xpath(
                    firstNode,
                    operationsURLExpr,
                    XPathConstants.STRING, context);
        }
        return null;
    }

    private String loadXMLFilter(String search) {
        StringWriter writer = new StringWriter();
        try {

            InputStream stream =
                    Misc.getResource(CSW_QUERY_FILEPATH);
            IOUtils.copy(stream, writer, "UTF-8");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        String xmlStr = writer.toString();
        return xmlStr.replace("{SUCHBEGRIFF}", search);
    }

    /**
     * returns the catalog url.
     * @return the url of the catalog
     */
    public URL getUrl() {
        return this.catalogURL;
    }
}
