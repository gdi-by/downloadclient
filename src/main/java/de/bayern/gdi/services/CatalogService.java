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

import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.XML;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
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
    private static final Logger log
            = Logger.getLogger(CatalogService.class.getName());
    private URL catalogURL;
    private String userName;
    private static final int MIN_SEARCHLENGTH = 2;
    private String password;
    private String providerName;
    private NamespaceContext context;

    /**
     * Constructor.
     * @param url String URL
     * @param userName Username
     * @param password Password
     * @throws MalformedURLException
     */
    public CatalogService(String url, String userName, String password) throws
            MalformedURLException {
        this(new URL(url), userName, password);
    }

    /**
     * Constructor.
     * @param url Stirng URL
     * @throws MalformedURLException When URL has bad format
     */
    public CatalogService(String url) throws MalformedURLException {
        this(url, null, null);
    }

    /**
     * Constructor.
     * @param url URL
     */
    public CatalogService(URL url) {
        this(url, null, null);
    }

    /**
     * Constructor.
     * @param url URL
     * @param userName Username
     * @param password Password
     */
    public CatalogService(URL url, String userName, String password) {
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
        String getProviderExpr = "//ows:ServiceIdentification/ows:Title";
        Node providerNameNode = (Node) XML.xpath(xml,
                getProviderExpr,
                XPathConstants.NODE,
                context);
        this.providerName = providerNameNode.getTextContent();
    }

    /**
     * gets the Name of the service Provider.
     * @return Name of the service Provider
     */
    public String getProviderName() {
        return this.providerName;
    }

    /**
     * retrieves a Map of ServiceNames and URLs for a Filter-Word.
     * @param filter the Word to filter to
     * @return Map of Service Names and URLs
     */
    public Map<String, String> getServicesByFilter(String filter) {
        Map<String, String> map = new HashMap<>();
        if (filter.length() > MIN_SEARCHLENGTH) {
            URL requestURL = setURLRequestAndSearch(filter);
            Document xml = XML.getDocument(requestURL,
                    this.userName,
                    this.password);
            String numberOfResultsExpr =
                    "//csw:SearchResults/@numberOfRecordsMatched";
            Double numberOfResults = (Double) XML.xpath(xml,
                    numberOfResultsExpr,
                    XPathConstants.NUMBER, this.context);
            String nodeListOfServicesExpr =
                    "//csw:SearchResults//gmd:MD_Metadata";
            NodeList servicesNL = (NodeList) XML.xpath(xml,
                    nodeListOfServicesExpr,
                    XPathConstants.NODESET, this.context);
            if (numberOfResults.intValue() == servicesNL.getLength()) {
                for (int i = 0; i < numberOfResults; i++) {
                    Node serviceN = servicesNL.item(i);
                    String nameExpr =
                            "gmd:identificationInfo" +
                                    "/srv:SV_ServiceIdentification" +
                                    "/gmd:citation" +
                                    "/gmd:CI_Citation" +
                                    "/gmd:title" +
                                    "/gco:CharacterString";
                    String serviceName = (String) XML.xpath(serviceN,
                            nameExpr,
                            XPathConstants.STRING, context);

                    String typeExpr =
                            "gmd:identificationInfo" +
                                    "/srv:SV_ServiceIdentification" +
                                    "/srv:serviceTypeVersion" +
                                    "/gco:CharacterString";
                    String serviceType = (String) XML.xpath(serviceN,
                            typeExpr,
                            XPathConstants.STRING, context);

                    String urlExpr =
                            "gmd:distributionInfo" +
                                    "/gmd:MD_Distribution" +
                                    "/gmd:transferOptions" +
                                    "/gmd:MD_DigitalTransferOptions" +
                                    "/gmd:onLine" +
                                    "/gmd:CI_OnlineResource" +
                                    "/gmd:linkage" +
                                    "/gmd:URL";
                    String serviceURL = (String) XML.xpath(serviceN,
                            urlExpr,
                            XPathConstants.STRING, context);
                    if(serviceType.toUpperCase().contains("WFS")
                            || serviceType.toUpperCase().contains("ATOM")
                            || serviceType.toUpperCase().contains("DOWNLOAD")) {
                        serviceURL = makeCapabiltiesURL(serviceURL,
                                serviceType);
                        map.put(serviceName, serviceURL);
                    }
                }
            }
        }
        return map;
    }

    private String makeCapabiltiesURL(String url, String type) {
        if (url.endsWith("?") && type.toUpperCase().contains("WFS")) {
            return url + "service=wfs&version=" + getVersionOfType(type) +
                    "&request=GetCapabilities";
        }
        if (!url.toUpperCase().contains("GETCAPABILITIES") && type
                .toUpperCase().contains("WFS")) {
            return url + "?service=wfs&version=" + getVersionOfType(type) +
                    "request=GetCapabilities";
        }
        return url;
    }

    private String getVersionOfType(String type) {
        String versionNumber = type.substring(type.lastIndexOf(" ") + 1);
        return versionNumber;
    }
    private URL setURLRequestAndSearch(String search) {
        URL newURL = null;
        try {
            newURL = new URL(this.catalogURL.toString().replace(
                    "GetCapabilities", "GetRecords"
                            + "&version=2.0.2"
                            + "&namespace=xmlns"
                            + "(csw=" + CSW_NAMESPACE + "),"
                            + "xmlns(gmd=" + GMD_NAMESPACE + ")"
                            + "&resultType=results"
                            + "&outputFormat=application/xml"
                            + "&outputSchema=" + GMD_NAMESPACE
                            + "&startPosition=1"
                            + "&maxRecords=20"
                            + "&typeNames=csw:Record"
                            + "&elementSetName=full"
                            + "&constraintLanguage=CQL_TEXT"
                            + "&constraint_language_version=1.1.0"
                            + "&constraint=csw:AnyText='" + search + "'"));
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return newURL;
    }

}
