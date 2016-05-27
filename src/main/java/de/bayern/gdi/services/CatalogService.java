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

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.StringUtils;
import de.bayern.gdi.utils.XML;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import net.opengis.cat.csw20.CapabilitiesType;
import net.opengis.ows10.ServiceProviderType;
import org.geotools.csw.CSWConfiguration;
import org.geotools.xml.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class CatalogService {

    private static final String CSW_NAMESPACE =
            "http://www.opengis.net/cat/csw/2.0.2";
    private static final String GMD_NAMESPACE =
            "http://www.isotc211.org/2005/gmd";
    private static final Logger log
            = Logger.getLogger(CatalogService.class.getName());
    private URL catalogURL;
    private String userName;
    private static final int MIN_SEARCHLENGTH = 2;
    private String password;
    private ServiceProviderType serviceProvider;

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
        Object parsed = getParsedObject(this.catalogURL);
        CapabilitiesType servciceCapType = (CapabilitiesType) parsed;
        this.serviceProvider = servciceCapType.getServiceProvider();
    }

    /**
     * gets the Name of the service Provider.
     * @return Name of the service Provider
     */
    public String getProviderName() {
        return this.serviceProvider.getProviderName();
    }

    /**
     * retrieves a Map of ServiceNames and URLs for a Filter-Word.
     * @param filter the Word to filter to
     * @return Map of Service Names and URLs
     */
    public Map<String, String> getServicesByFilter(String filter) {
        Map<String, String> map = new HashMap<>();
        if (filter.length() > MIN_SEARCHLENGTH) {
            System.out.println("Search for: " + filter);
            URL requestURL = setURLRequestAndSearch(filter);
            /* FIXME - WHEN TRYING TO USE IMPLEMENTED STUFF EXCEPTION RISES:
            java.lang.NoSuchMethodException:
            net.opengis.cat.csw20.impl.Csw20FactoryImpl
                .createAbstractRecordType()
            Use this piece of code to retrieve expected Excpetion
            Object parsed = this.getParsedObject(requestURL);
            GetRecordsResponseType records = (GetRecordsResponseType) parsed;
            SearchResultsType searchResults = records.getSearchResults();
            for (AbstractRecordType recordType : searchResults.getAbstractRecord
                    ()) {
                System.out.println(recordType.toString());
            }
            */

            Document xml = XML.getDocument(requestURL,
                    this.userName,
                    this.password);
            String searchResultExpression =
                    "//*[local-name()='SearchResults']";
            NamespaceContext context = new NamespaceContextMap(
                    "csw", CSW_NAMESPACE,
                    "gmd", GMD_NAMESPACE);
            Node searchResultsNode = (Node) XML.xpath(xml,
                    searchResultExpression,
                    XPathConstants.NODE, context);
            NamedNodeMap searchResultsNodeAttributes =
                    searchResultsNode.getAttributes();
            Node searchResultsNodeAttributesRecordsMatchedItem =
                    searchResultsNodeAttributes.getNamedItem(
                            "numberOfRecordsMatched");
            int numberOfRecordsMatched =
                    Integer.parseInt(
                            searchResultsNodeAttributesRecordsMatchedItem
                    .getTextContent());
            if (numberOfRecordsMatched > 0) {
                //System.out.println("More than one Result found");
                String identificationExpression =
                        "//*[local-name()='identificationInfo']";
                DTMNodeList identificationNL = (DTMNodeList) XML.xpath(
                        searchResultsNode,
                        identificationExpression,
                        XPathConstants.NODESET, context);
                //System.out.println(identificationNL.getLength());
                String transferoptionsExprssion =
                        "//*[local-name()='transferOptions']";
                DTMNodeList transferoptionsNL = (DTMNodeList) XML.xpath(
                        searchResultsNode,
                        transferoptionsExprssion,
                        XPathConstants.NODESET, context);
                //System.out.println(transferoptionsNL.getLength());
                if (identificationNL.getLength() == transferoptionsNL
                        .getLength()) {
                    log.log(Level.INFO, "Found " + numberOfRecordsMatched
                            + " Entries in the Catalog",
                            numberOfRecordsMatched);
                    String characterStringExpression =
                            "//*[local-name()='CharacterString']";
                    for(int i = 0; i < numberOfRecordsMatched; i++) {
                        Node identificationN = identificationNL.item(i);
                        Node transferoptinN = transferoptionsNL.item(i);
                        String titleExpression =
                                "//*[local-name()='title']";
                        Node titlteNode = (Node) XML.xpath(identificationN,
                                titleExpression,
                                XPathConstants.NODE, context);
                        Node titleCharStringNode = XML.getChildWithName
                                (titlteNode, "gco:CharacterString");
                        String title= titleCharStringNode.getTextContent();
                        Node digitalTransferOptionsNode = XML.getChildWithName
                                (transferoptinN,
                                        "gmd:MD_DigitalTransferOptions");
                        Node onLineNode = XML.getChildWithName
                                (digitalTransferOptionsNode, "gmd:onLine");

                        Node onlineRessourceNode = XML.getChildWithName
                                (onLineNode, "gmd:CI_OnlineResource");
                        Node linkageNode = XML.getChildWithName
                                (onlineRessourceNode,
                                        "gmd:linkage");
                        Node urlNode = XML.getChildWithName(linkageNode,
                                "gmd:URL");
                        String url = urlNode.getTextContent();
                        url = makeCapabiltiesURL(url);
                        map.put(title, url);
                    }
                }
            }

        }
        return map;
    }

    private String makeCapabiltiesURL(String url) {
        if(url.endsWith("?")) {
            url = url + "service=wfs&request=GetCapabilities";
        }
        return url;
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

    private Object getParsedObject(URL url) {
        Object parsed = null;
        try {
            CSWConfiguration configuration =
                    new CSWConfiguration();
            Parser parser = new Parser(configuration);
            URLConnection conn = null;
            if (url.toString().toLowerCase().startsWith("https")) {
                HttpsURLConnection con
                        = (HttpsURLConnection) url.openConnection();
                conn = (URLConnection) con;
            } else {
                conn = url.openConnection();
            }
            if (StringUtils.getBase64EncAuth(
                    this.userName, this.password) != null) {
                conn.setRequestProperty("Authorization", "Basic "
                        + StringUtils.getBase64EncAuth(
                        this.userName, this.password));
            }
            InputStream is = conn.getInputStream();
            InputSource xml = new InputSource(is);
            parsed = parser.parse(xml);
        } catch (IOException
                | SAXException
                | ParserConfigurationException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return parsed;
    }
}
